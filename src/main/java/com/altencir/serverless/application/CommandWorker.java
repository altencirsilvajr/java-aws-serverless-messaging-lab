package com.altencir.serverless.application;

import com.altencir.serverless.domain.ProcessingStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Clock;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CommandWorker {
    private static final Logger LOG = Logger.getLogger(CommandWorker.class);
    private final ProcessingStateStore states;
    private final CommandQueue queue;
    private final Clock clock;
    private final Counter completed;
    private final Counter failed;
    private final Counter duplicates;

    @Inject
    public CommandWorker(ProcessingStateStore states, CommandQueue queue, Clock clock, MeterRegistry meters) {
        this(states, queue, clock,
                meters.counter("commands.completed"),
                meters.counter("commands.failed"),
                meters.counter("commands.duplicates"));
    }

    CommandWorker(ProcessingStateStore states, CommandQueue queue, Clock clock) {
        this(states, queue, clock, null, null, null);
    }

    private CommandWorker(ProcessingStateStore states, CommandQueue queue, Clock clock,
            Counter completed, Counter failed, Counter duplicates) {
        this.states = states;
        this.queue = queue;
        this.clock = clock;
        this.completed = completed;
        this.failed = failed;
        this.duplicates = duplicates;
    }

    @Scheduled(every = "${commands.worker.interval:1s}", concurrentExecution = ConcurrentExecution.SKIP)
    public void poll() {
        for (var received : queue.receive(10, 1)) {
            process(received);
        }
    }

    private void process(ReceivedCommand received) {
        var id = received.command().messageId();
        var current = states.find(id);
        if (current.isPresent() && current.get().status() == ProcessingStatus.COMPLETED) {
            states.recordDuplicate(id, clock.instant());
            queue.delete(received.receiptHandle());
            increment(duplicates);
            LOG.infof("duplicate command acknowledged messageId=%s", id);
            return;
        }
        if (!states.markProcessing(id, clock.instant())) {
            LOG.warnf("command state unavailable messageId=%s", id);
            return;
        }
        try {
            if (received.command().simulateFailure()) {
                throw new IllegalStateException("simulated processing failure");
            }
            states.markCompleted(id, clock.instant());
            queue.delete(received.receiptHandle());
            increment(completed);
            LOG.infof("command completed messageId=%s", id);
        } catch (RuntimeException error) {
            states.markFailed(id, error.getMessage(), clock.instant());
            increment(failed);
            LOG.errorf(error, "command failed and remains visible for SQS redrive messageId=%s", id);
        }
    }

    private static void increment(Counter counter) {
        if (counter != null) counter.increment();
    }
}
