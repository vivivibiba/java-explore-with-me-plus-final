package ru.practicum.explorewithme.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
public @interface DateIsNotEarly {
    String message() default "Время начала события должно начинаться не ранее, чем через два часа";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int value() default 2;
}