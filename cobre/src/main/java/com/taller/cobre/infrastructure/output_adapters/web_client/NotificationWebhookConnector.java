package com.taller.cobre.infrastructure.output_adapters.web_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.taller.cobre.domain.model.client.WebhookResponse;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import reactor.core.publisher.Mono;

public interface NotificationWebhookConnector {
    Mono<WebhookResponse> sendNotification(String url, String secretKey, JsonNode payload);
    Mono<WebhookResponse> sendNotification(String url, String secretKey, NotificationDomain payload);
}
