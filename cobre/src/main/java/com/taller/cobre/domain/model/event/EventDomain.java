package com.taller.cobre.domain.model.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.With;

import java.time.LocalDateTime;
@With
public record EventDomain(
    long id,
    long clientId,
    EventType eventType,
    JsonNode details,
    LocalDateTime createdAt
) {

}
