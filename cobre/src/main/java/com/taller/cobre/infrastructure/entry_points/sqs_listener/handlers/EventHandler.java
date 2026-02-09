package com.taller.cobre.infrastructure.entry_points.sqs_listener.handlers;

import com.taller.cobre.domain.model.mappers.DomainMapper;
import com.taller.cobre.domain.use_case.NotificationGateway;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.lang.IO.println;

@Service
public class EventHandler {
    private final NotificationGateway notificationGateway;
    private final DomainMapper mapper;

    public EventHandler(NotificationGateway notificationGateway, DomainMapper mapper) {
        this.notificationGateway = notificationGateway;
        this.mapper = mapper;
    }


    public Mono<Void>handleEvent(EventMessage eventMessage){
        println("event");
        var eventDomain = mapper.toEventDomain(eventMessage);
        return notificationGateway.sendNotification(eventDomain);
    }

    public Mono<Void>handleRetry(EventMessage eventMessage){
        println("retry");
        return Mono.empty();
    }
}
