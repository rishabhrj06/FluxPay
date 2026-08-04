package com.fluxpay.payment.processor.strategy;

import com.fluxpay.common.utlis.RandomizerUtil;
import com.fluxpay.payment.processor.PaymentProcessor;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;

public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse process(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@ptsbi";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPA_REJECTED", "Bank rejected the payment registration for payment id: " + request.paymentId());
        }

        String processorRef = "UPI_" + RandomizerUtil.randomBase64(16);
        String bankRef = "BANK_REF" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Success(processorRef, bankRef);
    }
}
