package com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("clients")
public class Client {
    @Id
    private Integer id;
    private String name;
    private String url;
    private String subscriptions;
    private String secretKey;
    private String email;
}