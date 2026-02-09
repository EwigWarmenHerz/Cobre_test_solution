package com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos;

import java.util.Map;

public record EventMessage(
    String eventId,
    int clientId,
    String eventType,
    Map<String, Object> payload,
    int retryCount
) {

    public EventMessage nextAttempt(){
        return new EventMessage(
            eventId,
            clientId,
            eventType,
            payload,
            retryCount + 1
        );
    }
}
