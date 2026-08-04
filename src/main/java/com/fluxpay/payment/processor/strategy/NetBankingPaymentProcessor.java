package com.fluxpay.payment.processor.strategy;

import com.fluxpay.common.utlis.RandomizerUtil;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import com.fluxpay.payment.processor.PaymentProcessor;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse process(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("BANK").toString() : null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED", "Bank rejected the payment registration for payment id: " + request.paymentId());
        }

        String processorRef = "NBK_" + RandomizerUtil.randomBase64(16);
        String redirectRef = "http://localhost:8080/payments/" + processorRef;

        return new PaymentProcessorResponse.Success(processorRef, redirectRef);
    }
}
