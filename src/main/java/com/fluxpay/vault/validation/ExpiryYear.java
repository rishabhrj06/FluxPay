package com.fluxpay.vault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {ExpiryYearValidator.class}
)
public @interface ExpiryYear {
    String message() default "Card has expired";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
