package com.fluxpay.merchant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MerchantRegistrationRequest(
        @NotBlank String name,
        @Email String email
) {}
