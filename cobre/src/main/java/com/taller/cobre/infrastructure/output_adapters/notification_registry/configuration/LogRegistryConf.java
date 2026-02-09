package com.taller.cobre.infrastructure.output_adapters.notification_registry.configuration;

import com.taller.cobre.util.AwsParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
public class LogRegistryConf {
    private final AwsParameters awsParameters;

    public LogRegistryConf(AwsParameters awsParameters) {
        this.awsParameters = awsParameters;
    }

    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient(){
        return DynamoDbAsyncClient.builder()
            .endpointOverride(URI.create(awsParameters.endpoint()))
            .region(Region.of(awsParameters.region()))
            .credentialsProvider(getCredentialsProvider())
            .build();

    }

    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider
            .create(AwsBasicCredentials.create(awsParameters.accessKey(), awsParameters.secretKey()));
    }
}
