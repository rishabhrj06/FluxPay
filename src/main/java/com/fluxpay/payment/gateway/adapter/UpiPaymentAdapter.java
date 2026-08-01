package com.fluxpay.payment.gateway.adapter;

import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;

public class UpiPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentResult paymentInitiate(PaymentRequest request) {
        return null;
    }
}
