package com.fluxpay.merchant.dto.request;

import com.fluxpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
