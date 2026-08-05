package com.altencir.serverless.infrastructure;

import com.altencir.serverless.application.ProcessingStateStore;
import com.altencir.serverless.domain.ProcessingRecord;
import com.altencir.serverless.domain.ProcessingStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@ApplicationScoped
public class DynamoDbProcessingStateStore implements ProcessingStateStore {
    private final DynamoDbClient dynamo;
    private final String table;

    public DynamoDbProcessingStateStore(DynamoDbClient dynamo,
            @ConfigProperty(name = "aws.dynamodb.table") String table) {
        this.dynamo = dynamo;
        this.table = table;
    }

    @Override
    public Optional<ProcessingRecord> find(String messageId) {
        var response = dynamo.getItem(builder -> builder.tableName(table).key(key(messageId)).consistentRead(true));
        return response.hasItem() && !response.item().isEmpty() ? Optional.of(fromItem(response.item())) : Optional.empty();
    }

    @Override
    public boolean create(ProcessingRecord record) {
        try {
            dynamo.putItem(builder -> builder.tableName(table).item(toItem(record))
                    .conditionExpression("attribute_not_exists(messageId)"));
            return true;
        } catch (ConditionalCheckFailedException duplicate) {
            return false;
        }
    }

    @Override
    public boolean markProcessing(String messageId, Instant at) {
        try {
            dynamo.updateItem(builder -> builder.tableName(table).key(key(messageId))
                    .conditionExpression("attribute_exists(messageId) AND #status <> :completed")
                    .updateExpression("SET #status = :processing, updatedAt = :at ADD attempts :one")
                    .expressionAttributeNames(Map.of("#status", "status"))
                    .expressionAttributeValues(Map.of(
                            ":completed", text(ProcessingStatus.COMPLETED.name()),
                            ":processing", text(ProcessingStatus.PROCESSING.name()),
                            ":at", text(at.toString()),
                            ":one", number(1))));
            return true;
        } catch (ConditionalCheckFailedException ignored) {
            return false;
        }
    }

    @Override
    public void markCompleted(String messageId, Instant at) {
        setState(messageId, ProcessingStatus.COMPLETED, null, at);
    }

    @Override
    public void markFailed(String messageId, String error, Instant at) {
        setState(messageId, ProcessingStatus.FAILED, error, at);
    }

    @Override
    public void recordDuplicate(String messageId, Instant at) {
        dynamo.updateItem(builder -> builder.tableName(table).key(key(messageId))
                .updateExpression("SET updatedAt = :at ADD duplicateCount :one")
                .expressionAttributeValues(Map.of(":at", text(at.toString()), ":one", number(1))));
    }

    private void setState(String id, ProcessingStatus status, String error, Instant at) {
        var values = new HashMap<String, AttributeValue>();
        values.put(":status", text(status.name()));
        values.put(":at", text(at.toString()));
        if (error == null) {
            dynamo.updateItem(builder -> builder.tableName(table).key(key(id))
                    .updateExpression("SET #status = :status, updatedAt = :at REMOVE lastError")
                    .expressionAttributeNames(Map.of("#status", "status"))
                    .expressionAttributeValues(values));
        } else {
            values.put(":error", text(error));
            dynamo.updateItem(builder -> builder.tableName(table).key(key(id))
                    .updateExpression("SET #status = :status, updatedAt = :at, lastError = :error")
                    .expressionAttributeNames(Map.of("#status", "status"))
                    .expressionAttributeValues(values));
        }
    }

    private static Map<String, AttributeValue> key(String id) {
        return Map.of("messageId", text(id));
    }

    private static Map<String, AttributeValue> toItem(ProcessingRecord record) {
        var item = new HashMap<String, AttributeValue>();
        item.put("messageId", text(record.messageId()));
        item.put("payload", text(record.payload()));
        item.put("status", text(record.status().name()));
        item.put("attempts", number(record.attempts()));
        item.put("duplicateCount", number(record.duplicateCount()));
        item.put("simulateFailure", AttributeValue.builder().bool(record.simulateFailure()).build());
        item.put("createdAt", text(record.createdAt().toString()));
        item.put("updatedAt", text(record.updatedAt().toString()));
        return item;
    }

    private static ProcessingRecord fromItem(Map<String, AttributeValue> item) {
        return new ProcessingRecord(
                item.get("messageId").s(), item.get("payload").s(), ProcessingStatus.valueOf(item.get("status").s()),
                Integer.parseInt(item.get("attempts").n()), Integer.parseInt(item.get("duplicateCount").n()),
                item.get("simulateFailure").bool(), item.containsKey("lastError") ? item.get("lastError").s() : null,
                Instant.parse(item.get("createdAt").s()), Instant.parse(item.get("updatedAt").s()));
    }

    private static AttributeValue text(String value) { return AttributeValue.builder().s(value).build(); }
    private static AttributeValue number(int value) { return AttributeValue.builder().n(Integer.toString(value)).build(); }
}
