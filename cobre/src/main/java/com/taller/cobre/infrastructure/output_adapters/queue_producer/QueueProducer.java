package com.taller.cobre.infrastructure.output_adapters.queue_producer;

import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.util.AwsParameters;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class QueueProducer {
    private final SqsTemplate sqsTemplate;
    private final AwsParameters awsParameters;

    public QueueProducer(SqsTemplate sqsTemplate, AwsParameters awsParameters) {
        this.sqsTemplate = sqsTemplate;
        this.awsParameters = awsParameters;
    }

    public Mono<Void> sendToRetry(NotificationDomain notification) {
        return Mono.fromFuture(() ->
                sqsTemplate.sendAsync(to -> to
                    .queue(awsParameters.retryQueue())
                    .payload(notification) // Enviamos el objeto de notificación
                    .header("retry-count", notification.tries()) // Opcional: header útil para SQS
                )
            ).doOnSuccess(m -> log.info("Notificación {} enviada a Retry Queue. Intento: {}",
                notification.id(), notification.tries()))
            .then();
    }

    public Mono<Void> sendToDLQ(NotificationDomain notification) {
        return Mono.fromFuture(() ->
                sqsTemplate.sendAsync(to -> to
                    .queue(awsParameters.dlqQueue())
                    .payload(notification)
                )
            ).doOnSuccess(m -> log.error("Notificación {} enviada a DLQ tras agotar reintentos",
                notification.id()))
            .then();
    }
}
