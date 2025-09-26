package com.example.bankcards.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DistinctCardsValidator.class)
public @interface DistinctCards {
    String message() default "fromCardId и toCardId не должны совпадать";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String fromField() default "fromCardId";
    String toField() default "toCardId";
}

