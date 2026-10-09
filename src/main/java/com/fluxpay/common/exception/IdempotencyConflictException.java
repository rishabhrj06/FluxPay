package com.fluxpay.common.exception;

import lombok.Getter;

@Getter
public class IdempotencyConflictException extends RuntimeException {
    private final String message;

    public IdempotencyConflictException(String message) {
        super(message);
        this.message = message;
    }
}
