package com.example.bankcards.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class DistinctCardsValidator implements ConstraintValidator<DistinctCards, Object> {
    private String fromField;
    private String toField;

    @Override
    public void initialize(DistinctCards constraintAnnotation) {
        this.fromField = constraintAnnotation.fromField();
        this.toField = constraintAnnotation.toField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        try {
            Field from = value.getClass().getDeclaredField(fromField);
            Field to = value.getClass().getDeclaredField(toField);
            from.setAccessible(true);
            to.setAccessible(true);
            Object fromVal = from.get(value);
            Object toVal = to.get(value);
            if (fromVal == null || toVal == null) return true; // другие валидаторы сработают отдельно
            return !fromVal.equals(toVal);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return true; // если поля нет — не валидируем
        }
    }
}

