package com.taller.cobre.infrastructure.entry_points.sqs_listener;

import com.taller.cobre.util.AwsParameters;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.listener.errorhandler.AsyncErrorHandler;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
@EnableConfigurationProperties(AwsParameters.class)
public class SQSConfig {

    private final AwsParameters awsParameters;

    public SQSConfig(AwsParameters awsParameters) {
        this.awsParameters = awsParameters;
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(){
        return SqsAsyncClient.builder()
            .endpointOverride(URI.create(awsParameters.endpoint()))
            .region(Region.of(awsParameters.region()))
            .credentialsProvider(getCredentialsProvider())
            .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient){
        return SqsTemplate.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .configure(options -> {
                options.acknowledgementMode(AcknowledgementMode.ON_SUCCESS);
            })
            .errorHandler(handleErrorsSqS())
            .build();
    }

    private AsyncErrorHandler<Object> handleErrorsSqS() {
        return new AsyncErrorHandler<Object>() {
            @Override
            public CompletableFuture<Void> handle(Message<Object> message, Throwable t) {
                log.error("Error asíncrono en SQS Listener. Payload: {}", message.getPayload());
                log.error("Causa de la excepción: ", t.getCause() != null ? t.getCause() : t);
                return CompletableFuture.completedFuture(null);
            }
        };
    }


    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider
            .create(AwsBasicCredentials.create(awsParameters.accessKey(), awsParameters.secretKey()));
    }
}
