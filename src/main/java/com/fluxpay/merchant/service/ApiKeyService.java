package com.fluxpay.merchant.service;

import com.fluxpay.merchant.dto.request.CreateApiKeyRequest;
import com.fluxpay.merchant.dto.response.ApiKeyCreateResponse;
import com.fluxpay.merchant.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse createApiKey(UUID merchantId, CreateApiKeyRequest request);

    List<ApiKeyResponse> getAllApiKeys(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotateApiKey(UUID merchantId, UUID keyId);
}
