package com.taller.cobre.infrastructure.entry_points.reactive_web.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.taller.cobre.domain.model.enums.NotificationStatus;
import com.taller.cobre.domain.model.event.EventDomain;
import com.taller.cobre.domain.model.event.EventType;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;
@With
@Builder
public record NotificationResponse(
    long id,
    NotificationStatus status,
    int tries,
    LocalDateTime updatedAt,
    EventType eventType,
    JsonNode jsonNode
) {
}
