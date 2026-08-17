package ru.practicum.explorewithme.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateValidator implements ConstraintValidator<DateIsNotEarly, LocalDateTime> {
    private int hours;

    @Override
    public void initialize(DateIsNotEarly constraintAnnotation) {
        this.hours = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return !value.isBefore(LocalDateTime.now().plusHours(hours).minusSeconds(1));
    }
}