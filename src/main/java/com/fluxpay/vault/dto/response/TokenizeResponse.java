package com.fluxpay.vault.dto.response;

import com.fluxpay.common.enums.CardBrand;

public record TokenizeResponse(
        String token,
        CardBrand brand,
        String lastFour,
        Integer expiryMonth,
        Integer expiryYear
) {
}
