package com.taller.cobre.infrastructure.output_adapters.web_client;

import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.output_adapters.queue_producer.QueueProducer;
import reactor.core.publisher.Mono;

public class ErrorHandler {

    private final QueueProducer producer;

    public ErrorHandler(QueueProducer producer) {
        this.producer = producer;
    }

    public Mono<Void>handleClientFailures(NotificationDomain notificationDomain){
        return Mono.empty();
//        var retryMessage = notificationDomain.nextAttempt();
//        if(notificationDomain.re() <= 5){
//            return producer.sendToRetry(retryMessage);
//        }
//        return producer.sendToDLQ(retryMessage);
    }
}
