package com.taller.cobre.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cloud.aws")
public record AwsParameters(
    String region,
    String accessKey,
    String secretKey,
    String endpoint,
    String logRegistryTable,
    String eventsQueue,
    String retryQueue,
    String dlqQueue
) {
}
