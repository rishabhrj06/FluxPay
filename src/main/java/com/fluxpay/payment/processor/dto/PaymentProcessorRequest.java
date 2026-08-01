package com.fluxpay.payment.processor.dto;

import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.PaymentMethod;

import java.util.Map;

public record PaymentProcessorRequest(
        PaymentMethod method,
        Map<String, Object> methodDetails,
        Money amount
) {
}
