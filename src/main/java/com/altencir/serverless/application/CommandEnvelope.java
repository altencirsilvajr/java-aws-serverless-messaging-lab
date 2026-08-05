package com.altencir.serverless.application;

public record CommandEnvelope(String messageId, String payload, boolean simulateFailure) { }
