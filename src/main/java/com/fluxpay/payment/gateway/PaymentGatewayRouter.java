package com.fluxpay.payment.gateway;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.config.PaymentAdapterConfig;
import com.fluxpay.payment.gateway.dto.PaymentRequest;
import com.fluxpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;
    private final PaymentAdapterConfig paymentAdapterConfig;

    public PaymentResult initiatePayment(PaymentRequest request){
        PaymentAdapter adapter = paymentAdapters.get(request.method());
        if(adapter == null){
            throw new IllegalArgumentException("Invalid request method" + request.method());
        }
        return adapter.paymentInitiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter adapter = paymentAdapters.get(method);
        if(adapter == null){
            throw new IllegalArgumentException("Invalid request method" + method);
        }
        return adapter.capture(paymentId);
    }
}
