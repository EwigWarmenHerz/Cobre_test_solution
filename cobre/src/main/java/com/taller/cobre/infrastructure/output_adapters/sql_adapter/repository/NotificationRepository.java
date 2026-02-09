package com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository;

import com.taller.cobre.infrastructure.output_adapters.sql_adapter.dtos.NotificationWithEvent;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {
    Flux<Notification> findByEventId(Integer eventId);
    Flux<Notification>findByClientId(long clientId);
    @Query("""
        SELECT
        n.id           AS id,
        n.status       AS status,
        n.tries        AS tries,
        n.updated_at   AS updatedAt,
        e.event_type   AS eventType,
        e.details      AS eventDetails
        FROM notifications n
        INNER JOIN events e ON n.event_id = e.id
        WHERE n.id = :id
    """)
    Mono<NotificationWithEvent> findByNotificationIdWithEventData(Long id);

    @Query("""
        SELECT n.id, n.status, n.tries, n.updated_at, 
               e.event_type, e.details 
        FROM notifications n 
        INNER JOIN events e ON n.event_id = e.id 
        WHERE n.client_id = :clientId 
        ORDER BY n.updated_at DESC
    """)
    Flux<NotificationWithEvent> findAllWithEventDataByClientId(Long clientId);

}
