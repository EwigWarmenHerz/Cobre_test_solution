package com.taller.cobre.domain.use_case;

import com.taller.cobre.domain.model.event.EventDomain;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.reactive_web.dtos.NotificationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> sendNotification(EventDomain eventDomain);
    Mono<NotificationResponse> getNotificationById(String secretKey, String notificationId);
    Flux<NotificationResponse> getAllNotifications(String secretKey);
    Mono<Boolean> retryNotification(String secretKey, long eventId);
    Mono<Void> retryNotification(NotificationDomain eventDomain);
}
