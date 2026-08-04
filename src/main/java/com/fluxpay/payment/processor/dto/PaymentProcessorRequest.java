package com.fluxpay.payment.processor.dto;

import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID processId,
        UUID paymentId,
        String pan,
        String expiry,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        Money amount
) {
    public static PaymentProcessorRequest forCard(UUID paymentId, String pan, String expiry, Map<String, Object> methodDetails, Money amount){
        return new PaymentProcessorRequest(UUID.randomUUID(), paymentId, pan, expiry, PaymentMethod.CARD, methodDetails, amount);
    }

    public static PaymentProcessorRequest forNonCard(UUID paymentId, Money amount, Map<String, Object> methodDetails, PaymentMethod method){
        return new PaymentProcessorRequest(UUID.randomUUID(), paymentId, null, null, method, methodDetails, amount);
    }
}
