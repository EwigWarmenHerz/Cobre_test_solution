package com.taller.cobre.domain.model.client;

import com.taller.cobre.domain.model.event.EventType;
import software.amazon.awssdk.services.sqs.endpoints.internal.Value;

import java.util.Set;

public record ClientRouting(
    int id,
    String url,
    String secretKey,
    Set<EventType> subscribedEvents
) {

    public boolean isSubscribedTo(EventType eventType){
        return subscribedEvents.contains(eventType);
    }
}
