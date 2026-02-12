package com.taller.cobre.infrastructure.entry_points.sqs_listener;

import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.handlers.EventHandler;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.lang.IO.println;
import static reactor.netty.http.HttpConnectionLiveness.log;

@Component
public class QueueListeners {

    private final EventHandler eventHandler;

    public QueueListeners(EventHandler eventHandler){
        this.eventHandler = eventHandler;

    }

    @SqsListener("notification-event-queue")
    public void processEvenQueue(EventMessage message){
        eventHandler.handleEvent(message)
            .subscribe(
            null,
            error -> log.error("Error procesando mensaje de SQS", error),
            () -> log.info("Flujo de notificación completado")
        );
    }


    @SqsListener("notification-retry-queue")
    public void processRetries(NotificationDomain notification) {
        eventHandler.handleRetry(notification)
            .subscribe(
                null,
                error -> log.error("Error procesando mensaje de SQS", error),
                () -> log.info("Flujo de notificación completado"));
    }
}
