package com.altencir.serverless.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.altencir.serverless.domain.ProcessingRecord;
import com.altencir.serverless.domain.ProcessingStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommandServiceTest {
    private final MemoryStateStore states = new MemoryStateStore();
    private final MemoryQueue queue = new MemoryQueue();
    private final CommandService service = new CommandService(states, queue,
            Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void acceptsAndPublishesANewCommand() {
        var result = service.submit(new SubmitCommand("msg-1", "rebuild-index", false));

        assertThat(result.duplicate()).isFalse();
        assertThat(result.record().status()).isEqualTo(ProcessingStatus.ACCEPTED);
        assertThat(queue.published).containsKey("msg-1");
    }

    @Test
    void returnsExistingStateWithoutPublishingDuplicateHttpCommand() {
        service.submit(new SubmitCommand("msg-1", "rebuild-index", false));

        var duplicate = service.submit(new SubmitCommand("msg-1", "different", false));

        assertThat(duplicate.duplicate()).isTrue();
        assertThat(queue.publishCount).isEqualTo(1);
    }

    static final class MemoryStateStore implements ProcessingStateStore {
        final Map<String, ProcessingRecord> records = new HashMap<>();
        public Optional<ProcessingRecord> find(String id) { return Optional.ofNullable(records.get(id)); }
        public boolean create(ProcessingRecord record) { return records.putIfAbsent(record.messageId(), record) == null; }
        public boolean markProcessing(String id, Instant at) { return false; }
        public void markCompleted(String id, Instant at) { }
        public void markFailed(String id, String error, Instant at) { }
        public void recordDuplicate(String id, Instant at) { }
    }

    static final class MemoryQueue implements CommandQueue {
        final Map<String, CommandEnvelope> published = new HashMap<>();
        int publishCount;
        public void publish(CommandEnvelope command) { published.put(command.messageId(), command); publishCount++; }
        public java.util.List<ReceivedCommand> receive(int limit, int waitSeconds) { return java.util.List.of(); }
        public void delete(String receiptHandle) { }
    }
}
