package com.altencir.serverless.application;

import com.altencir.serverless.domain.ProcessingRecord;
import java.time.Instant;
import java.util.Optional;

public interface ProcessingStateStore {
    Optional<ProcessingRecord> find(String messageId);
    boolean create(ProcessingRecord record);
    boolean markProcessing(String messageId, Instant at);
    void markCompleted(String messageId, Instant at);
    void markFailed(String messageId, String error, Instant at);
    void recordDuplicate(String messageId, Instant at);
}
