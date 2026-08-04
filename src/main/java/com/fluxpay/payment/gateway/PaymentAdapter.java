package com.fluxpay.payment.gateway;

import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {
    PaymentResult paymentInitiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
