package com.taller.cobre.infrastructure.output_adapters.sql_adapter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.r2dbc")
public record ConnectionProperties(
    String url,
    String username,
    String password
) {
}