package com.taller.cobre.domain.model.client;

public record WebhookResponse(
    int statusCode,
    String responseBody,
    boolean isSuccess
) {
}
