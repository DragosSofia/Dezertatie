package com.example.demo.validators;

import com.example.demo.Configuration.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import com.example.demo.dto.request.AdditionalQueryInfo;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, AdditionalQueryInfo> {

    @Override
    public boolean isValid(AdditionalQueryInfo value, ConstraintValidatorContext context) {
        if (value.getStartDate() == null || value.getEndDate() == null) {
            return true; // @NotNull handles missing fields
        }

        return value.getStartDate().isBefore(value.getEndDate());
    }
}