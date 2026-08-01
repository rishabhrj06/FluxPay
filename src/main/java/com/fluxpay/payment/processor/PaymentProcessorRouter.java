package com.fluxpay.payment.processor;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.processor.dto.PaymentProcessorRequest;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PaymentProcessorRouter {

    private Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    PaymentProcessorResponse process(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessors.get(request.method());
        if(processor == null){
            throw new IllegalArgumentException("Payment Processor not Registered for method: " + request.method());
        }
        return processor.process(request);
    }
}
