package com.altencir.serverless.domain;

import java.time.Instant;

public record ProcessingRecord(
        String messageId,
        String payload,
        ProcessingStatus status,
        int attempts,
        int duplicateCount,
        boolean simulateFailure,
        String lastError,
        Instant createdAt,
        Instant updatedAt) {

    public static ProcessingRecord accepted(String messageId, String payload, boolean simulateFailure, Instant at) {
        return new ProcessingRecord(messageId, payload, ProcessingStatus.ACCEPTED, 0, 0, simulateFailure, null, at, at);
    }

    public ProcessingRecord processing(Instant at) {
        return new ProcessingRecord(messageId, payload, ProcessingStatus.PROCESSING, attempts + 1,
                duplicateCount, simulateFailure, null, createdAt, at);
    }

    public ProcessingRecord completed(Instant at) {
        return new ProcessingRecord(messageId, payload, ProcessingStatus.COMPLETED, attempts,
                duplicateCount, simulateFailure, null, createdAt, at);
    }

    public ProcessingRecord failed(String error, Instant at) {
        return new ProcessingRecord(messageId, payload, ProcessingStatus.FAILED, attempts,
                duplicateCount, simulateFailure, error, createdAt, at);
    }

    public ProcessingRecord duplicate(Instant at) {
        return new ProcessingRecord(messageId, payload, status, attempts,
                duplicateCount + 1, simulateFailure, lastError, createdAt, at);
    }
}
