package com.fluxpay.payment.gateway.adapter;

import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CardPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentResult paymentInitiate(PaymentRequest request) {
        return null;
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
