package com.fluxpay.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failure, PaymentResult.Pending{

    record Pending(String registrationRef) implements PaymentResult {}
    record Failure(String errorCode, String errorDescription) implements PaymentResult{}
}
