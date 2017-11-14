package uk.gov.hmcts.cmccase.models.constraints;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateNotInThePastConstraintValidator implements ConstraintValidator<DateNotInThePast, LocalDate> {

    @Override
    public void initialize(DateNotInThePast dateNotInThePast) {
        // NO-OP
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext cxt) {
        // Another validator should check for this if required
        if (localDate == null) {
            return true;
        }

        return !localDate.isBefore(LocalDate.now());
    }
}
