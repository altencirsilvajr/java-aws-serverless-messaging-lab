package com.altencir.serverless.infrastructure;

import com.altencir.serverless.application.CommandEnvelope;
import com.altencir.serverless.application.CommandQueue;
import com.altencir.serverless.application.ReceivedCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@ApplicationScoped
public class SqsCommandQueue implements CommandQueue {
    private final SqsClient sqs;
    private final ObjectMapper json;
    private final String queueUrl;
    private final String dlqUrl;

    public SqsCommandQueue(SqsClient sqs, ObjectMapper json,
            @ConfigProperty(name = "aws.sqs.queue-url") String queueUrl,
            @ConfigProperty(name = "aws.sqs.dlq-url") String dlqUrl) {
        this.sqs = sqs;
        this.json = json;
        this.queueUrl = queueUrl;
        this.dlqUrl = dlqUrl;
    }

    @Override
    public void publish(CommandEnvelope command) {
        try {
            var body = json.writeValueAsString(command);
            sqs.sendMessage(builder -> builder.queueUrl(queueUrl).messageBody(body));
        } catch (JsonProcessingException error) {
            throw new IllegalArgumentException("Command cannot be serialized", error);
        }
    }

    @Override
    public List<ReceivedCommand> receive(int limit, int waitSeconds) {
        return sqs.receiveMessage(builder -> builder.queueUrl(queueUrl).maxNumberOfMessages(limit)
                        .waitTimeSeconds(waitSeconds).visibilityTimeout(2))
                .messages().stream().map(message -> {
                    try {
                        return new ReceivedCommand(message.receiptHandle(), json.readValue(message.body(), CommandEnvelope.class));
                    } catch (JsonProcessingException error) {
                        throw new IllegalArgumentException("Invalid command envelope", error);
                    }
                }).toList();
    }

    @Override
    public void delete(String receiptHandle) {
        sqs.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(receiptHandle));
    }

    @Override
    public long pendingMessages() {
        return depth(queueUrl);
    }

    @Override
    public long deadLetterMessages() {
        return depth(dlqUrl);
    }

    private long depth(String url) {
        var response = sqs.getQueueAttributes(GetQueueAttributesRequest.builder().queueUrl(url)
                .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES).build());
        return Long.parseLong(response.attributes().getOrDefault(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "0"));
    }
}
