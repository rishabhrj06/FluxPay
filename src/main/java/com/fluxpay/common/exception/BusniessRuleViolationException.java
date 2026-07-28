package com.fluxpay.common.exception;

import lombok.Getter;

@Getter
public class BusniessRuleViolationException extends RuntimeException {

    private final String errorCode;

    public BusniessRuleViolationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
