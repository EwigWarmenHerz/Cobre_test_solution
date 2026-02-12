package com.taller.cobre.infrastructure.entry_points.reactive_web.handlers;

import com.taller.cobre.domain.model.exceptions.BusinessException;
import com.taller.cobre.domain.use_case.NotificationGateway;
import com.taller.cobre.infrastructure.entry_points.reactive_web.dtos.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.taller.cobre.domain.model.constants.CommonConstants.SECRET_KEY_INPUT_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAdminsitrationHandlerTest {

    @Mock private NotificationGateway notificationGateway;
    @Mock private ServerRequest serverRequest;
    @Mock private ServerRequest.Headers headers;

    @InjectMocks
    private NotificationAdminsitrationHandler handler;

    private final String VALID_KEY = "test-secret-123";

    @BeforeEach
    void setup() {
        lenient().when(serverRequest.headers()).thenReturn(headers);
        lenient().when(headers.firstHeader(SECRET_KEY_INPUT_HEADER)).thenReturn(VALID_KEY);
    }

    @Test
    void getAllClientNotifications_Success() {
        when(notificationGateway.getAllNotifications(VALID_KEY)).thenReturn(Flux.just(NotificationResponse.builder().id(1L).build()));

        StepVerifier.create(handler.getAllClientNotifications(serverRequest))
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
            .verifyComplete();
    }

    @Test
    void getNotificationByNotificationId_Success() {
        String id = "500";
        when(serverRequest.pathVariable(anyString())).thenReturn(id);
        when(notificationGateway.getNotificationById(VALID_KEY, id)).thenReturn(Mono.just(NotificationResponse.builder().id(500L).build()));

        StepVerifier.create(handler.getNotificationByNotificationId(serverRequest))
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
            .verifyComplete();
    }

    @Test
    void retryNotification_Success() {
        String id = "500";
        when(serverRequest.pathVariable(anyString())).thenReturn(id);
        when(notificationGateway.retryNotification(VALID_KEY, 500L)).thenReturn(Mono.just(true));

        StepVerifier.create(handler.retryNotification(serverRequest))
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.OK))
            .verifyComplete();
    }
}
