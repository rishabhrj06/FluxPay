package com.fluxpay.merchant.dto.response;

import com.fluxpay.common.enums.BusinessType;
import com.fluxpay.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(

        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
