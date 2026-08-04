package com.fluxpay.payment.gateway.adapter;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import com.fluxpay.payment.processor.PaymentProcessorRouter;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult paymentInitiate(PaymentRequest request) {
        log.info("Payment initiated from NetBankingAdapter for payment id: {}", request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.forNonCard(
                    request.paymentId(),
                    request.amount(),
                    request.methodDetails(),
                    PaymentMethod.NET_BANKING
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.process(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());

                case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());

                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch(Exception e) {
            log.error("NetBankingAdapter failed for payment id: {}", request.paymentId());
            return new PaymentResult.Failure("NET_BANKING FAILED", e.getMessage());
        }

    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NBK_REF");
    }
}
