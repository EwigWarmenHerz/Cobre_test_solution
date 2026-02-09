package com.taller.cobre.domain.model.exceptions;

import com.taller.cobre.domain.model.enums.TechnicalMessages;

public abstract class EventsException extends RuntimeException {
    public final Throwable throwable;
    public EventsException(String message, Throwable throwable) {
        super(message);
        this.throwable = throwable;
    }
}
