package com.altencir.serverless.application;

public record ReceivedCommand(String receiptHandle, CommandEnvelope command) { }
