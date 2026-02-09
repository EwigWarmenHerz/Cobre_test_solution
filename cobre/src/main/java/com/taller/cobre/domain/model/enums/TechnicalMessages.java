package com.taller.cobre.domain.model.enums;

public enum TechnicalMessages {

    INTERNAL_ERROR(500, "Internal error");

    public final int statusCode;
    public final String message;

    TechnicalMessages(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
