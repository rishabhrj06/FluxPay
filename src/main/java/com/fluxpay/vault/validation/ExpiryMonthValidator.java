package com.fluxpay.vault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExpiryMonthValidator implements ConstraintValidator<ExpiryMonth, Integer> {

    @Override
    public boolean isValid(Integer month, ConstraintValidatorContext constraintValidatorContext) {
        if(month == null){
            return true;
        }

        return month >= 1 && month <= 12;
    }
}
