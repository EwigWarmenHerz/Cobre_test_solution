package com.taller.cobre.infrastructure.output_adapters.web_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.taller.cobre.domain.model.client.WebhookResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WebhookClientTest {

    private MockWebServer mockWebServer;
    private WebhookClient webhookClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();


        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
        webhookClient = new WebhookClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void sendNotification_Success() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"message\": \"received\"}")
            .addHeader("Content-Type", "application/json"));
        JsonNode payload = JsonNodeFactory.instance.objectNode().put("data", "test");
        Mono<WebhookResponse> result = webhookClient.sendNotification("/", "secret-key", payload);
        StepVerifier.create(result)
            .expectNextMatches(response -> {
                assertNotNull(response.responseBody(), "El body de respuesta no debe ser nulo");
                assertEquals(200, response.statusCode());
                assertTrue(response.isSuccess());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void sendNotification_ServerError() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));
        var emptyPayload = JsonNodeFactory.instance.objectNode();
        var result = webhookClient.sendNotification("/", "secret-key", emptyPayload);
        StepVerifier.create(result)
            .expectNextMatches(response -> {
                assertNotNull(response.responseBody(), "Incluso en error, el body debe ser una cadena (ej. mensaje de error)");
                assertEquals(500, response.statusCode());
                assertFalse(response.isSuccess());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void sendNotification_Timeout() {

        mockWebServer.enqueue(new MockResponse()
            .setBody("Slow response")
            .setBodyDelay(11, TimeUnit.SECONDS));
        var payload = JsonNodeFactory.instance.objectNode().put("info", "timeout_test");
        var result = webhookClient.sendNotification("/", "secret-key", payload);

        StepVerifier.create(result)
            .expectNextMatches(response -> {
                assertNotNull(response.responseBody(), "El body debe contener el mensaje del timeout");
                assertEquals(500, response.statusCode());
                assertFalse(response.isSuccess());
                return true;
            })
            .verifyComplete();
    }
}