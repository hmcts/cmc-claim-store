package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateNotInThePastConstraintValidator implements ConstraintValidator<DateNotInThePast, LocalDate> {

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext cxt) {
        // Another validator should check for this if required
        if (localDate == null) {
            return true;
        }

        return !localDate.isBefore(LocalDate.now());
    }
}
