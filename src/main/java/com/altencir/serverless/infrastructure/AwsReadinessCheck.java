package com.altencir.serverless.infrastructure;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Readiness
public class AwsReadinessCheck implements HealthCheck {
    private final DynamoDbClient dynamo;
    private final String table;

    public AwsReadinessCheck(DynamoDbClient dynamo, @ConfigProperty(name = "aws.dynamodb.table") String table) {
        this.dynamo = dynamo;
        this.table = table;
    }

    @Override
    public HealthCheckResponse call() {
        try {
            dynamo.describeTable(builder -> builder.tableName(table));
            return HealthCheckResponse.up("aws-resources");
        } catch (RuntimeException error) {
            return HealthCheckResponse.named("aws-resources").down().withData("reason", error.getClass().getSimpleName()).build();
        }
    }
}
