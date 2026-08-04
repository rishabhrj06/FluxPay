package com.fluxpay.payment.processor.strategy;

import com.fluxpay.payment.processor.PaymentProcessor;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse process(PaymentProcessorRequest request) {
        return null;
    }
}
