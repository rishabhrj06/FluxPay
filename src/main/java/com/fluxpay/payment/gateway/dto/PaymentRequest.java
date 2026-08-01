package com.fluxpay.payment.gateway.dto;

import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID merchantId,
        UUID orderId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
