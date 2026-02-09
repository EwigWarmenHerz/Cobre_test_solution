package com.taller.cobre.infrastructure.output_adapters.web_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.taller.cobre.domain.model.client.WebhookResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class WebhookClient implements NotificationWebhookConnector {

    private final WebClient client;

    public WebhookClient(WebClient client) {
        this.client = client;
    }

    public Mono<WebhookResponse> sendNotification(String url, String secretKey, JsonNode payload) {
        return client.post()
            .uri(url)
            .header("x-notification-Signature", secretKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchangeToMono(response ->
                response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(responseBody -> new WebhookResponse(
                        response.statusCode().value(),
                        responseBody,
                        response.statusCode().is2xxSuccessful()
                    ))
            )
            .timeout(Duration.ofSeconds(10))
            .onErrorResume(e -> {
                log.error("Request error {}: {}", url, e.getMessage());
                return Mono.just(new WebhookResponse(500, e.getMessage(), false));
            });
    }
}
