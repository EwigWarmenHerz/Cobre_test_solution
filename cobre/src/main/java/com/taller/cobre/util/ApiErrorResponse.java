package com.taller.cobre.util;

import lombok.Builder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Builder
public record ApiErrorResponse(
    String message,
    int errorCode,
    LocalDateTime timestamp,
    int status
) {
}
