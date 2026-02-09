package com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities;

import com.taller.cobre.domain.model.enums.NotificationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
@With
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notifications")
public class Notification {
    @Id
    private long id;

    @Column("client_id")
    private long clientId;

    @Column("event_id")
    private long eventId;

    @Column("status")
    private NotificationStatus status;

    private Integer tries;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
