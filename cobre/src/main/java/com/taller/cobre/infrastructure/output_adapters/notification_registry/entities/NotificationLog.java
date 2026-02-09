package com.taller.cobre.infrastructure.output_adapters.notification_registry.entities;

import com.taller.cobre.domain.model.enums.NotificationStatus;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class NotificationLog {
    String eventId;
    Long timestamp;
    long clientId;
    NotificationStatus status;
    int retryCount;
    String errorMessage;
}
