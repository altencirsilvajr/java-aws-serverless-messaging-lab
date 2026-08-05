package com.altencir.serverless.application;

public final class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String messageId) {
        super("Command not found: " + messageId);
    }
}
