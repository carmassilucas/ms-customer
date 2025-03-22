package com.ecosystem.ms_customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class AwsConfig {

    private final String endpoint;

    private final String accessKey;

    private final String secretKey;

    public AwsConfig(@Value("${aws.localstack.endpoint}") String endpoint,
                     @Value("${aws.localstack.access-key}") String accessKey,
                     @Value("${aws.localstack.secret-key}") String secretKey) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Bean
    public DynamoDbClient configureDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(this.endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(this.accessKey, this.secretKey)
                ))
                .build();
    }

    @Bean
    public S3Client configureS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(this.endpoint))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(this.accessKey, this.accessKey)
                ))
                .serviceConfiguration(S3Configuration.builder().build())
                .build();
    }
}
