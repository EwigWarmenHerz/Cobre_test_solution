package com.taller.cobre.infrastructure.entry_points.reactive_web;

import com.taller.cobre.infrastructure.entry_points.reactive_web.handlers.NotificationAdminsitrationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.taller.cobre.infrastructure.entry_points.reactive_web.routes.Routes.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse>routerFunction(NotificationAdminsitrationHandler administrationHandler){
        return route()
            .GET(NOTIFICATION_EVENTS, administrationHandler::getAllClientNotifications)
            .GET(NOTIFICATION_EVENTS_BY_ID, administrationHandler::getNotificationByNotificationId )
            .POST(NOTIFICATION_RESEND, administrationHandler::retryNotification)
            .build();
    }
}
