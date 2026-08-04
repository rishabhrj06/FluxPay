package com.fluxpay.payment.service;

import com.fluxpay.payment.dto.request.PaymentInitRequest;
import com.fluxpay.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request);

    PaymentResponse capture(UUID merchantId, UUID paymentId);
}
