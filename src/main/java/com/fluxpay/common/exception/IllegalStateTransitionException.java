package com.fluxpay.common.exception;

import lombok.Getter;

@Getter
public class IllegalStateTransitionException extends RuntimeException {

    private final String fromState;
    private final String toEvent;

    public IllegalStateTransitionException(String fromState, String event) {
        super("Invalid Transition from " + fromState + " with " + event);
        this.fromState = fromState;
        this.toEvent = event;
    }
}
