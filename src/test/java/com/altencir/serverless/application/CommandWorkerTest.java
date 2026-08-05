package com.altencir.serverless.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.altencir.serverless.domain.ProcessingRecord;
import com.altencir.serverless.domain.ProcessingStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommandWorkerTest {
    @Test
    void completesOneCommandAndDeletesItsMessage() {
        var states = new MemoryStateStore(ProcessingRecord.accepted("msg-1", "work", false, Instant.EPOCH));
        var queue = new MemoryQueue(new ReceivedCommand("receipt-1", new CommandEnvelope("msg-1", "work", false)));
        var worker = new CommandWorker(states, queue, Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC));

        worker.poll();

        assertThat(states.records.get("msg-1").status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(queue.deleted).containsExactly("receipt-1");
    }

    @Test
    void observesFailureAndLeavesMessageForSqsRedrive() {
        var states = new MemoryStateStore(ProcessingRecord.accepted("msg-1", "work", true, Instant.EPOCH));
        var queue = new MemoryQueue(new ReceivedCommand("receipt-1", new CommandEnvelope("msg-1", "work", true)));
        var worker = new CommandWorker(states, queue, Clock.systemUTC());

        worker.poll();

        assertThat(states.records.get("msg-1").status()).isEqualTo(ProcessingStatus.FAILED);
        assertThat(queue.deleted).isEmpty();
    }

    @Test
    void acknowledgesACompletedDuplicateWithoutProcessingAgain() {
        var completed = new ProcessingRecord("msg-1", "work", ProcessingStatus.COMPLETED, 1, 0, false, null, Instant.EPOCH, Instant.EPOCH);
        var states = new MemoryStateStore(completed);
        var queue = new MemoryQueue(new ReceivedCommand("receipt-1", new CommandEnvelope("msg-1", "work", false)));
        var worker = new CommandWorker(states, queue, Clock.systemUTC());

        worker.poll();

        assertThat(states.records.get("msg-1").duplicateCount()).isEqualTo(1);
        assertThat(queue.deleted).containsExactly("receipt-1");
    }

    static final class MemoryStateStore implements ProcessingStateStore {
        final Map<String, ProcessingRecord> records = new HashMap<>();
        MemoryStateStore(ProcessingRecord record) { records.put(record.messageId(), record); }
        public Optional<ProcessingRecord> find(String id) { return Optional.ofNullable(records.get(id)); }
        public boolean create(ProcessingRecord record) { return records.putIfAbsent(record.messageId(), record) == null; }
        public boolean markProcessing(String id, Instant at) {
            var current = records.get(id);
            if (current.status() == ProcessingStatus.COMPLETED) return false;
            records.put(id, current.processing(at)); return true;
        }
        public void markCompleted(String id, Instant at) { records.put(id, records.get(id).completed(at)); }
        public void markFailed(String id, String error, Instant at) { records.put(id, records.get(id).failed(error, at)); }
        public void recordDuplicate(String id, Instant at) { records.put(id, records.get(id).duplicate(at)); }
    }

    static final class MemoryQueue implements CommandQueue {
        final List<ReceivedCommand> messages;
        final List<String> deleted = new ArrayList<>();
        MemoryQueue(ReceivedCommand message) { messages = List.of(message); }
        public void publish(CommandEnvelope command) { }
        public List<ReceivedCommand> receive(int limit, int waitSeconds) { return messages; }
        public void delete(String receiptHandle) { deleted.add(receiptHandle); }
    }
}
