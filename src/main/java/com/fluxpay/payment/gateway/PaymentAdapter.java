package com.fluxpay.payment.gateway;

import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;

public interface PaymentAdapter {
    PaymentResult paymentInitiate(PaymentRequest request);
}
