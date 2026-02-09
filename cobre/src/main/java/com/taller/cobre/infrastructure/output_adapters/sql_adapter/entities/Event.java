package com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@Table("events")
public class Event {
    @Id
    private long id;

    @Column("client_id")
    private long clientId;

    private JsonNode details;

    @Column("event_type")
    private Integer eventType;

    @Column("created_at")
    private LocalDateTime createdAt;
}