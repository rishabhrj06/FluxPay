package com.fluxpay.vault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ExpiryYearValidator implements ConstraintValidator<ExpiryYear, Integer> {

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext constraintValidatorContext) {
        if(year == null){
            return true;
        }

        return year >= 2000 && year <= 9999;
    }
}
