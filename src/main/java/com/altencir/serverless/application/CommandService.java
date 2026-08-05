package com.altencir.serverless.application;

import com.altencir.serverless.domain.ProcessingRecord;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.util.Optional;

@ApplicationScoped
public class CommandService {
    private final ProcessingStateStore states;
    private final CommandQueue queue;
    private final Clock clock;

    public CommandService(ProcessingStateStore states, CommandQueue queue, Clock clock) {
        this.states = states;
        this.queue = queue;
        this.clock = clock;
    }

    public SubmissionResult submit(SubmitCommand command) {
        var record = ProcessingRecord.accepted(
                command.messageId(), command.payload(), command.simulateFailure(), clock.instant());
        if (!states.create(record)) {
            return new SubmissionResult(states.find(command.messageId()).orElseThrow(), true);
        }
        queue.publish(new CommandEnvelope(command.messageId(), command.payload(), command.simulateFailure()));
        return new SubmissionResult(record, false);
    }

    public Optional<ProcessingRecord> find(String messageId) {
        return states.find(messageId);
    }

    public void publishDuplicate(String messageId) {
        var record = states.find(messageId).orElseThrow(() -> new CommandNotFoundException(messageId));
        queue.publish(new CommandEnvelope(record.messageId(), record.payload(), record.simulateFailure()));
    }
}
