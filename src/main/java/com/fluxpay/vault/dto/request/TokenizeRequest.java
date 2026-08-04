package com.fluxpay.vault.dto.request;

import com.fluxpay.vault.validation.ExpiryMonth;
import com.fluxpay.vault.validation.ExpiryYear;
import com.fluxpay.vault.validation.ValidateExpiryDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

@ValidateExpiryDate
public record TokenizeRequest(

        @NotBlank(message = "Card number is required")
        @LuhnCheck(message = "Card is Invalid")
        @Pattern(regexp = "//d{13,19}", message = "Card number should be between 13 to 19 characters")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "//d{3,4}", message = "CVV should contain 3 or 4 digits")
        String cvv,

        @NotNull(message = "Expiry month is required")
        @ExpiryMonth
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @ExpiryYear
        Integer expiryYear,

        @NotNull(message = "customer ID is required")
        UUID customerId,

        @NotBlank(message = "Card holder name is required")
        String cardHolderName
) {
}
