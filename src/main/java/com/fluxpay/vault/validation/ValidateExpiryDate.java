package com.fluxpay.vault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {ExpiryDateValidator.class}
)
public @interface ValidateExpiryDate {
    String message() default "Card has expired";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
