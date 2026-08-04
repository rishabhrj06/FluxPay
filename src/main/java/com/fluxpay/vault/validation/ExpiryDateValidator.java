package com.fluxpay.vault.validation;

import com.fluxpay.vault.dto.request.TokenizeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.YearMonth;

public class ExpiryDateValidator implements ConstraintValidator<ValidateExpiryDate, TokenizeRequest> {

    @Override
    public boolean isValid(TokenizeRequest tokenizeRequest, ConstraintValidatorContext constraintValidatorContext) {
        if(tokenizeRequest == null){
            return true;
        }

        YearMonth current = YearMonth.now();

        YearMonth entered = YearMonth.of(
                tokenizeRequest.expiryYear(),
                tokenizeRequest.expiryMonth()
        );

        return !entered.isBefore(current);
    }
}
