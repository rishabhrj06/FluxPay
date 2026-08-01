package com.fluxpay.payment.config;

import com.fluxpay.common.enums.PaymentMethod;
import com.fluxpay.payment.processor.PaymentProcessor;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import com.fluxpay.payment.processor.strategy.CardPaymentProcessor;
import com.fluxpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.fluxpay.payment.processor.strategy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor(),
                PaymentMethod.NET_BANKING, new NetBankingPaymentProcessor()
        );
    }
}
