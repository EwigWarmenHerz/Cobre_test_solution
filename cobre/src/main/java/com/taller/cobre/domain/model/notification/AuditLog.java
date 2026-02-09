package com.taller.cobre.domain.model.notification;

import com.taller.cobre.domain.model.enums.NotificationStatus;

import java.time.LocalDateTime;

public record AuditLog(
    String notificationId,
    String clientId,
    NotificationStatus status,
    String responseCode,
    String responseBody,
    String errorTrace,
    LocalDateTime timestamp
) {
}
