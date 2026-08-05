package com.altencir.serverless.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.net.URI;
import java.time.Clock;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@ApplicationScoped
public class AwsClients {
    @ConfigProperty(name = "aws.endpoint") URI endpoint;
    @ConfigProperty(name = "aws.region", defaultValue = "us-east-1") String region;
    @ConfigProperty(name = "aws.access-key", defaultValue = "test") String accessKey;
    @ConfigProperty(name = "aws.secret-key", defaultValue = "test") String secretKey;

    @Produces
    @ApplicationScoped
    Clock clock() {
        return Clock.systemUTC();
    }

    @Produces
    @ApplicationScoped
    SqsClient sqs() {
        return SqsClient.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Produces
    @ApplicationScoped
    DynamoDbClient dynamoDb() {
        return DynamoDbClient.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }
}
