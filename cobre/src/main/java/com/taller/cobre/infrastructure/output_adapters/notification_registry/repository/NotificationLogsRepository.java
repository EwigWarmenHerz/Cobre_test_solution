package com.taller.cobre.infrastructure.output_adapters.notification_registry.repository;

import com.taller.cobre.domain.model.notification.AuditLog;
import com.taller.cobre.infrastructure.output_adapters.notification_registry.entities.NotificationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class NotificationLogsRepository {
    private final DynamoDbAsyncClient dynamoClient;
    private static final String TABLE_NAME = "notification-logs";

    public NotificationLogsRepository(DynamoDbAsyncClient dynamoClient) {
        this.dynamoClient = dynamoClient;
    }

    public Mono<Void> saveLog(AuditLog audit) {
        var item = new HashMap<String, AttributeValue>();

        // Llave de partición (Partition Key)
        item.put("notificationId", AttributeValue.builder().s(audit.notificationId()).build());

        // Atributos
        item.put("clientId", AttributeValue.builder().n(String.valueOf(audit.clientId())).build());
        item.put("statusCode", AttributeValue.builder().n(String.valueOf(audit.status())).build());
        item.put("timestamp", AttributeValue.builder().s(audit.timestamp().toString()).build());

        // Manejo de nulos para campos opcionales
        if (audit.responseBody() != null) {
            item.put("responseBody", AttributeValue.builder().s(audit.responseBody()).build());
        }

        if (audit.errorTrace() != null) {
            item.put("errorTrace", AttributeValue.builder().s(audit.errorTrace()).build());
        } else {
            item.put("errorTrace", AttributeValue.builder().s("SUCCESS").build());
        }

        PutItemRequest request = PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(item)
            .build();

        return Mono.fromFuture(() -> dynamoClient.putItem(request))
            .doOnError(e -> log.error("Error persistiendo log en DynamoDB: {}", e.getMessage()))
            .then();
    }
}
