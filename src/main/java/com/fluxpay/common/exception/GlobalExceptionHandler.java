package com.fluxpay.common.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(ex.getErrorCode(), ex.getMessage())
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(ex.getResourceName().toUpperCase()+ "_NOT_FOUND: ", ex.getIdentifier())
        );
    }

    @ExceptionHandler(BusniessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusniessRuleViolationException(BusniessRuleViolationException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(ex.getErrorCode(), ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateTransitionException(IllegalStateTransitionException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(ex.getFromState(), ex.getToEvent())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                               HttpServletRequest request){
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldError(f.getField(), f.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of("VALIDATION_FAILED", "Request validation failed", fieldErrors)
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("INVALID_JWT_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.of("BAD_CREDENTIALS", "Password is invalid")
        );
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException ex){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-Rate-Limit-Remaining", "0")
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .header("X-Rate-Limit-Reset", String.valueOf(
                            Instant.now().plusSeconds(ex.getRetryAfterSeconds()).getEpochSecond()
                        ))
                .body(ErrorResponse.of("RATE_LIMIT_EXCEEDED", ex.getMessage())
        );
    }
}
