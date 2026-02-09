package com.taller.cobre.infrastructure.entry_points.reactive_web.routes;

public class Routes {
    public static final String NOTIFICATION_EVENTS = "/notification_events";
    public static final String NOTIFICATIONS_PATH_ID = "notification_event_id";
    public static final String NOTIFICATION_EVENTS_BY_ID = NOTIFICATION_EVENTS + "/{"+ NOTIFICATIONS_PATH_ID +"}";
    public static final String NOTIFICATION_RESEND = NOTIFICATION_EVENTS_BY_ID + "/replay";


}
