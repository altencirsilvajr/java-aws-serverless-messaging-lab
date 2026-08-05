package com.altencir.serverless.application;

public record SubmitCommand(String messageId, String payload, boolean simulateFailure) { }
