package com.ecosystem.ms_customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@TestConfiguration
public class AwsConfigTest {

    @Value("${aws.localstack.endpoint}")
    private String endpoint;

    @Value("${aws.localstack.access-key}")
    private String accessKey;

    @Value("${aws.localstack.secret-key}")
    private String secretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(this.endpoint))
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(this.accessKey, this.secretKey)
                ))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(this.endpoint))
                .region(Region.US_EAST_2)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(this.accessKey, this.secretKey)
                ))
                .serviceConfiguration(S3Configuration.builder().build())
                .build();
    }
}
