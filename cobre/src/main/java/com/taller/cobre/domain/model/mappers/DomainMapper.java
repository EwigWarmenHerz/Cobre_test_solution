package com.taller.cobre.domain.model.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.cobre.domain.model.client.ClientRouting;
import com.taller.cobre.domain.model.event.EventDomain;
import com.taller.cobre.domain.model.event.EventType;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Client;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Event;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DomainMapper {
    private final ObjectMapper mapper;

    protected DomainMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public  ClientRouting toClientRouting(Client clientEntity){
        var subscriptions = Arrays.stream(clientEntity.getSubscriptions().split(","))
            .map(String::trim)
            .map(EventType::valueOf)
            .collect(Collectors.toSet());
        return new ClientRouting(
            clientEntity.getId(),
            clientEntity.getUrl(),
            clientEntity.getSecretKey(),
            subscriptions
        );

    }
    public JsonNode mapToJsonNode(Map<String, Object> map){
        return mapper.valueToTree(map);
    }

    public  Map<String, Object> jsonNodeToMap(JsonNode node) {
        return mapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    }

    public EventDomain toEventDomain(EventMessage dto) {
        return new EventDomain(
            0,
            dto.clientId(),
            EventType.valueOf(dto.eventType()),
            mapper.valueToTree(dto.payload()),
            LocalDateTime.now()
        );
    }

    public Event toEventEntity(EventDomain eventDomain){
        return Event.builder()
            .id(eventDomain.id())
            .clientId(eventDomain.clientId())
            .payload(eventDomain.details())
            .eventType(eventDomain.eventType().name())
            .build();
    }

    public NotificationDomain toNotificationDomain(Notification entity, JsonNode payload) {
        if (entity == null) return null;

        return new NotificationDomain(
            entity.getId(),
            entity.getClientId(),
            entity.getEventId(),
            entity.getStatus(),
            payload,
            entity.getTries(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public Notification toNotificationEntity(NotificationDomain domain) {
        if (domain == null) return null;

        return Notification.builder()
            .id(domain.id())
            .eventId(domain.eventId())
            .clientId(domain.clientId())
            .status(domain.status())
            .tries(domain.tries())
            .createdAt(domain.createdAt())
            .updatedAt(domain.updatedAt())
            .build();
    }

    public EventDomain toEventDomain(Event entity) {
        if (entity == null) return null;
        return new EventDomain(
            entity.getId(),
            entity.getClientId(),
            EventType.valueOf(entity.getEventType()),
            entity.getPayload(),
            entity.getCreatedAt()
        );
    }
}
