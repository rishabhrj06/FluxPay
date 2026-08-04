package com.fluxpay.vault.service;

import com.fluxpay.vault.dto.request.TokenizeRequest;
import com.fluxpay.vault.dto.response.TokenizeResponse;

import java.net.URI;
import java.util.UUID;

public interface VaultService {

    TokenizeResponse createToken(UUID merchantId, TokenizeRequest request);
}
