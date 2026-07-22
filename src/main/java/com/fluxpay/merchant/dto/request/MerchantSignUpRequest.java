package com.fluxpay.merchant.dto.request;

import com.fluxpay.common.enums.BusinessType;
import jakarta.validation.constraints.*;

public record MerchantSignUpRequest(

        @NotNull
        @Size(max = 80, message = "Name can't be more than 80 character")
        String name,

        @Email
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @Size(min = 8, max = 16, message = "Password should be between 8 to 16 character")
        String password,

        @NotNull(message = "Business name is required")
        @Size(max = 200, message = "Business name should be under 200 character")
        String businessName,

        BusinessType businessType
) {
}
