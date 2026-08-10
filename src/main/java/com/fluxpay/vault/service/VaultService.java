package com.fluxpay.vault.service;

import com.fluxpay.common.entity.Money;
import com.fluxpay.payment.processor.dto.PaymentProcessorResponse;
import com.fluxpay.vault.dto.request.TokenizeRequest;
import com.fluxpay.vault.dto.response.TokenizeResponse;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public interface VaultService {

    TokenizeResponse createToken(UUID merchantId, TokenizeRequest request);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
