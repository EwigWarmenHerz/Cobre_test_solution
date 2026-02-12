package com.taller.cobre.util;

import com.taller.cobre.domain.model.exceptions.EventsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EventsException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> exceptionHandler(EventsException ex){
        return Mono.just(ResponseEntity
            .status(500)
            .body(ApiErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(500)
                .timestamp(LocalDateTime.now())
                .build()));
    }
}
