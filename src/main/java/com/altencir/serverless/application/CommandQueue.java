package com.altencir.serverless.application;

import java.util.List;

public interface CommandQueue {
    void publish(CommandEnvelope command);
    List<ReceivedCommand> receive(int limit, int waitSeconds);
    void delete(String receiptHandle);
    default long pendingMessages() { return 0; }
    default long deadLetterMessages() { return 0; }
}
