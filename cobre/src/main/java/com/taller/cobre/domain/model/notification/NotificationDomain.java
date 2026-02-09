package com.taller.cobre.domain.model.notification;

import com.taller.cobre.domain.model.enums.NotificationStatus;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;
@With
@Builder
public record NotificationDomain(
    long id,
    long clientId,
    long eventId,
    NotificationStatus status,
    Integer tries,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    private static final int maxRetires = 5;

    public boolean canRetry(){
        return this.tries < maxRetires;
    }

    public NotificationDomain updateForRetries(){
        return NotificationDomain.builder()
            .id(id)
            .clientId(clientId)
            .eventId(eventId)
            .status(status)
            .tries(tries +1)
            .createdAt(createdAt)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public NotificationDomain updateForDLQ(){
        return NotificationDomain.builder()
            .id(id)
            .clientId(clientId)
            .eventId(eventId)
            .status(NotificationStatus.FAILED)
            .tries(tries)
            .createdAt(createdAt)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
