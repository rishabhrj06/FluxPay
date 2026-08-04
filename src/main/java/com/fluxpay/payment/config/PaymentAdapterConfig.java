package com.fluxpay.payment.config;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.adapter.CardPaymentAdapter;
import com.fluxpay.payment.gateway.adapter.NetBankingAdapter;
import com.fluxpay.payment.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.UPI, upiPaymentAdapter,
                PaymentMethod.NET_BANKING, netBankingAdapter
        );
    }
}
