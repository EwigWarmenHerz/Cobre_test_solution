package com.taller.cobre.infrastructure.entry_points.reactive_web.handlers;

import com.taller.cobre.domain.model.exceptions.BusinessException;
import com.taller.cobre.domain.use_case.NotificationGateway;
import com.taller.cobre.util.HeaderExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.taller.cobre.infrastructure.entry_points.reactive_web.routes.Routes.NOTIFICATIONS_PATH_ID;

@Component
public class NotificationAdminsitrationHandler {

    private final NotificationGateway notificationGateway;

    public NotificationAdminsitrationHandler(NotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    public Mono<ServerResponse> getAllClientNotifications(ServerRequest request){
        var secretKey = HeaderExtractor.extractSecretKey(request);
        return notificationGateway.getAllNotifications(secretKey)
            .collectList()
            .flatMap(notificationDomains -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationDomains))
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(BusinessException.class, e ->
                ServerResponse.status(HttpStatus.FORBIDDEN).bodyValue(e.getMessage())
            );
    }

    public Mono<ServerResponse> getNotificationByNotificationId(ServerRequest request) {

        var secretKey = HeaderExtractor.extractSecretKey(request);
        var notificationIdStr = request.pathVariable(NOTIFICATIONS_PATH_ID);

        return notificationGateway.getNotificationById(secretKey, notificationIdStr)
            .flatMap(notification -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification))
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(BusinessException.class, e ->
                ServerResponse.status(HttpStatus.FORBIDDEN).bodyValue(e.getMessage())
            );
    }

    public Mono<ServerResponse>retryNotification(ServerRequest request){
        var secretKey = HeaderExtractor.extractSecretKey(request);
        var notificationIdStr = request.pathVariable(NOTIFICATIONS_PATH_ID);
        return notificationGateway.retryNotification(secretKey, Long.parseLong(notificationIdStr))
            .flatMap(notification -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification))
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume(BusinessException.class, e ->
                ServerResponse.status(HttpStatus.FORBIDDEN).bodyValue(e.getMessage())
            );

    }

    private Mono<ServerResponse>tellIfRetryWasSuccessful(boolean isSuccess){
        return isSuccess
            ? ServerResponse.ok().bodyValue(Map.of("Operation Status", "Success"))
            :ServerResponse.ok().bodyValue(Map.of("Operation Status:", "Failed"));
    }


}
