package com.fluxpay.payment.processor.strategy;

import com.fluxpay.common.utlis.RandomizerUtil;
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

    private static final String PAN_CARD_DECLINED = "4000000000000006";
    private static final String PAN_CARD_EXPIRED = "40000000000000069";

    @Override
    public PaymentProcessorResponse process(PaymentProcessorRequest request) {
        String pan = request.pan();

        if(pan.equals(PAN_CARD_DECLINED)){
            log.warn("Card Declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by bank");
        }
        if(pan.equals(PAN_CARD_EXPIRED)){
            log.warn("Card has Expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired");
        }

        String processorRef = "CARD_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
