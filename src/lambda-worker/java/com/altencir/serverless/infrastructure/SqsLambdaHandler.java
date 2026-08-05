package com.altencir.serverless.infrastructure;

import com.altencir.serverless.application.CommandEnvelope;
import com.altencir.serverless.application.ProcessingStateStore;
import com.altencir.serverless.domain.ProcessingStatus;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.time.Clock;

@Named("worker")
@ApplicationScoped
public class SqsLambdaHandler implements RequestHandler<SQSEvent, Void> {
    private final ProcessingStateStore states;
    private final ObjectMapper json;
    private final Clock clock;

    public SqsLambdaHandler(ProcessingStateStore states, ObjectMapper json, Clock clock) {
        this.states = states;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (var message : event.getRecords()) process(read(message.getBody()));
        return null;
    }

    private void process(CommandEnvelope command) {
        var current = states.find(command.messageId());
        if (current.isPresent() && current.get().status() == ProcessingStatus.COMPLETED) {
            states.recordDuplicate(command.messageId(), clock.instant());
            return;
        }
        if (!states.markProcessing(command.messageId(), clock.instant())) {
            throw new IllegalStateException("Command state unavailable: " + command.messageId());
        }
        if (command.simulateFailure()) {
            var error = "simulated processing failure";
            states.markFailed(command.messageId(), error, clock.instant());
            throw new IllegalStateException(error);
        }
        states.markCompleted(command.messageId(), clock.instant());
    }

    private CommandEnvelope read(String body) {
        try {
            return json.readValue(body, CommandEnvelope.class);
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("Invalid command envelope", error);
        }
    }
}
