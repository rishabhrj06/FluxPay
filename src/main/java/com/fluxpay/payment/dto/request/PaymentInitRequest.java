package com.fluxpay.payment.dto.request;

import com.fluxpay.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(

        @NotNull(message = "Order id required")
        UUID orderId,

        @NotNull(message = "Payment method required")
        PaymentMethod method,

        Map<String, Object> methodDetails

) {
}
