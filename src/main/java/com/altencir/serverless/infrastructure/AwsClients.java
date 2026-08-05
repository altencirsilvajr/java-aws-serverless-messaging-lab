package com.altencir.serverless.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.net.URI;
import java.time.Clock;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@ApplicationScoped
public class AwsClients {
    @ConfigProperty(name = "aws.endpoint") Optional<URI> endpoint;
    @ConfigProperty(name = "aws.region", defaultValue = "us-east-1") String region;
    @ConfigProperty(name = "aws.access-key") Optional<String> accessKey;
    @ConfigProperty(name = "aws.secret-key") Optional<String> secretKey;

    @Produces
    @ApplicationScoped
    Clock clock() {
        return Clock.systemUTC();
    }

    @Produces
    @ApplicationScoped
    SqsClient sqs() {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials())
                .httpClientBuilder(UrlConnectionHttpClient.builder());
        endpoint.ifPresent(builder::endpointOverride);
        return builder.build();
    }

    @Produces
    @ApplicationScoped
    DynamoDbClient dynamoDb() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials())
                .httpClientBuilder(UrlConnectionHttpClient.builder());
        endpoint.ifPresent(builder::endpointOverride);
        return builder.build();
    }

    private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider credentials() {
        if (accessKey.isPresent() && secretKey.isPresent()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey.get(), secretKey.get()));
        }
        return DefaultCredentialsProvider.create();
    }
}
