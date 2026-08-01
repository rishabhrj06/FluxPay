package com.fluxpay.payment.config;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.gateway.PaymentAdapter;
import com.fluxpay.payment.gateway.adapter.CardPaymentAdapter;
import com.fluxpay.payment.gateway.adapter.NetBankingAdapter;
import com.fluxpay.payment.gateway.adapter.UpiPaymentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentAdapterConfig {

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentAdapter(),
                PaymentMethod.UPI, new UpiPaymentAdapter(),
                PaymentMethod.NET_BANKING, new NetBankingAdapter()
        );
    }
}
