package com.fluxpay.payment.gateway.adapter;

import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import com.fluxpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult paymentInitiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");
        PaymentProcessorResponse response = vaultService.charge(
                request.paymentId(), token, request.amount(), request.methodDetails()
        );

        return switch(response) {
            case PaymentProcessorResponse.Success success ->  new PaymentResult.Success(
                    success.bankReference()
            );
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(
                    failure.errorCode(), failure.errorDescription()
            );
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
