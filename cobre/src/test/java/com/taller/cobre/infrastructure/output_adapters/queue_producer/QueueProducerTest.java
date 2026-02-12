package com.taller.cobre.infrastructure.output_adapters.queue_producer;

import com.taller.cobre.domain.model.enums.NotificationStatus;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.util.AwsParameters;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueProducerTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private AwsParameters awsParameters;

    @InjectMocks
    private QueueProducer queueProducer;

    private NotificationDomain notification;

    @BeforeEach
    void setUp() {
        notification = new NotificationDomain(
            500L, 100L, 1L, NotificationStatus.RETRYING, 1, null, null
        );


    }

    @Test
    void sendToRetry_ShouldSendWithPayload() {
        when(sqsTemplate.sendAsync(any(Consumer.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        var result = queueProducer.sendToRetry(notification);
        StepVerifier.create(result)
            .verifyComplete();

        verify(sqsTemplate).sendAsync(any(Consumer.class));
        assertNotNull(notification, "La notificación a enviar no debe ser nula");
    }

    @Test
    void sendToDLQ_ShouldSendWithPayload() {
        when(sqsTemplate.sendAsync(any(Consumer.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mono<Void> result = queueProducer.sendToDLQ(notification);
        StepVerifier.create(result)
            .verifyComplete();

        verify(sqsTemplate).sendAsync(any(Consumer.class));
        assertNotNull(notification.id(), "El ID de la notificación para DLQ no debe ser nulo");
    }

    @Test
    void sendToRetry_ShouldHandleError() {
        var future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("SQS Connection Failed"));

        when(sqsTemplate.sendAsync(any(Consumer.class))).thenReturn(future);


       var result = queueProducer.sendToRetry(notification);
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
}