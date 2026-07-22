package com.fluxpay.merchant.service;

import com.fluxpay.merchant.dto.request.MerchantSignUpRequest;
import com.fluxpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signUp(MerchantSignUpRequest request);
}
