package com.taller.cobre.infrastructure.entry_points.sqs_listener.handlers;

import com.taller.cobre.domain.model.mappers.DomainMapper;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.domain.use_case.NotificationGateway;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.lang.IO.println;

@Service
@Slf4j
public class EventHandler {
    private final NotificationGateway notificationGateway;
    private final DomainMapper mapper;

    public EventHandler(NotificationGateway notificationGateway, DomainMapper mapper) {
        this.notificationGateway = notificationGateway;
        this.mapper = mapper;
    }


    public Mono<Void>handleEvent(EventMessage eventMessage){
        var eventDomain = mapper.toEventDomain(eventMessage);
        log.info("Incoming event: " + eventDomain);
        return notificationGateway.sendNotification(eventDomain);
    }

    public Mono<Void>handleRetry(NotificationDomain notification){
       log.info("retry: {}", notification);
        return notificationGateway.retryNotification(notification);
    }
}
