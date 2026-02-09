package com.taller.cobre.domain.model.exceptions;

import com.taller.cobre.domain.model.enums.TechnicalMessages;

public class TechnicalException extends EventsException {

    public TechnicalException(String message, Throwable throwable) {
        super(message, throwable);

    }

}
