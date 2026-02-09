package com.taller.cobre.infrastructure.output_adapters.sql_adapter.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.taller.cobre.domain.model.enums.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationWithEvent(
    long id,
    NotificationStatus status,
    int tries,
    LocalDateTime updatedAt,
    String eventType,
    JsonNode details
) {
}
