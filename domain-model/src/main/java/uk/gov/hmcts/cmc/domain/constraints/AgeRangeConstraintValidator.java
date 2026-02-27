package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AgeRangeConstraintValidator implements ConstraintValidator<AgeRangeValidator, LocalDate> {

    private int minYears;
    private int maxYears;

    @Override
    public void initialize(AgeRangeValidator ageRangeValidator) {
        this.minYears = ageRangeValidator.minYears();
        this.maxYears = ageRangeValidator.maxYears();
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext cxt) {
        // Another validator should check for this if required
        if (localDate == null) {
            return true;
        }

        long years = getAge(localDate);

        return years >= minYears && years <= maxYears;
    }

    private long getAge(LocalDate localDate) {
        return ChronoUnit.YEARS.between(localDate, LocalDate.now());
    }
}
