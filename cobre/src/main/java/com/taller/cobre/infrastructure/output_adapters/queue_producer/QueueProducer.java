package com.taller.cobre.infrastructure.output_adapters.queue_producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper jsonMapper;

    public QueueProducer(SqsTemplate sqsTemplate, AwsParameters awsParameters, ObjectMapper jsonMapper) {
        this.sqsTemplate = sqsTemplate;
        this.awsParameters = awsParameters;
        this.jsonMapper = jsonMapper;
    }

    public Mono<Void> sendToRetry(NotificationDomain notification) {
        var body = serialize(notification);
        log.info("notificación que se mandara a cola de reintentos: {}", body);
        return Mono.fromFuture(() ->
                sqsTemplate.sendAsync(to -> to
                    .queue(awsParameters.retryQueue())
                    .payload(body)
                    .header("retry-count", notification.tries())
                )
            ).doOnSuccess(m -> log.info("Notificación {} enviada a Retry Queue. Intento: {}",
                notification.id(), notification.tries()))
            .then();
    }


    public Mono<Void> sendToDLQ(NotificationDomain notification) {
        var body = serialize(notification);
        log.info("notificación que se mandara a DLQ: {}", body);

        return Mono.fromFuture(() ->
                sqsTemplate.sendAsync(to -> to
                    .queue(awsParameters.dlqQueue())
                    .payload(body)
                )
            ).doOnSuccess(m -> log.error("Notificación {} enviada a DLQ tras agotar reintentos",
                notification.id()))
            .then();
    }

    private String serialize(NotificationDomain notification) {
        var body = "";
        try {
            body = jsonMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return body;
    }
}
