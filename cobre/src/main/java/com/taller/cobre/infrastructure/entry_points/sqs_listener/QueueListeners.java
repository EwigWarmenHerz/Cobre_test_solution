package com.taller.cobre.infrastructure.entry_points.sqs_listener;

import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.handlers.EventHandler;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.lang.IO.println;

@Component
public class QueueListeners {

    private final EventHandler eventHandler;

    public QueueListeners(EventHandler eventHandler){
        this.eventHandler = eventHandler;

    }

    @SqsListener("notification-event-queue")
    public Mono<Void> processEvenQueue(EventMessage message){
        return eventHandler.handleEvent(message);
    }


    @SqsListener("notification-retry-queue")
    public Mono<Void> processRetries(EventMessage message) {
        return eventHandler.handleRetry(message);
    }
}
