package com.fluxpay.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errorCode,
        Object errorDescription,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String errorCode, Object errorDescription){
        return new ErrorResponse(errorCode, errorDescription, LocalDateTime.now(), null);
    }

    public static ErrorResponse of(String errorCode, Object errorDescription, List<FieldError> fieldErrors){
        return new ErrorResponse(errorCode, errorDescription, LocalDateTime.now(), fieldErrors);
    }
}
