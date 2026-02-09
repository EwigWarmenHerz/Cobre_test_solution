package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.user.UserUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserUseCase userUseCase;


    @Override
    public Mono<Void> apply(Message message) {
        var messageBody = message.body();

        try {
            var userModel = objectMapper.readValue(messageBody, User.class);
            userUseCase.save(userModel)
                    .doOnSuccess(unused -> log.info("The user {} has been saved to DynamoDB",userModel))
                    .doOnError(error -> log.error(error.getMessage()))
                    .subscribe();

        } catch (JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }

        return Mono.empty();

    }
}
