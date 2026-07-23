package com.fluxpay.payment.dto.response;

import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID merchantId,
        String receipt,
        Money amount,
        OrderStatus status,
        Integer attempts,
        Map<String, Object> notes,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
