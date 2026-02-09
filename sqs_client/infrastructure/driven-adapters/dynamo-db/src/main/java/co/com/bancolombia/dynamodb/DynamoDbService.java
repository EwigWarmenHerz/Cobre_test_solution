package co.com.bancolombia.dynamodb;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service

public class DynamoDbService implements UserRepository {
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @Value("${aws.dynamodb.table_name}")
    private String tableName;

    public DynamoDbService(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }


    @Override
    public Mono<Void> create(User user) {
        var entity = new ModelEntity(user.id(), user.name(), user.surname(), user.email());
        var tableSchema = TableSchema.fromClass(ModelEntity.class);
        return Mono.fromFuture(() -> dynamoDbEnhancedAsyncClient.table(tableName, tableSchema)
                .putItem(entity))
                .then();
    }
}
