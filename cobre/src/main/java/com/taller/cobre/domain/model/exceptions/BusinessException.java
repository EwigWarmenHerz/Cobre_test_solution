package com.taller.cobre.domain.model.exceptions;

import com.taller.cobre.domain.model.enums.TechnicalMessages;

public class BusinessException extends EventsException{
    public BusinessException(String message) {
        super(message, null);
    }
}
