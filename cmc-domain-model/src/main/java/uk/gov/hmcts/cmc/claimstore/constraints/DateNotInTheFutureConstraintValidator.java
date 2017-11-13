package uk.gov.hmcts.cmc.claimstore.constraints;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateNotInTheFutureConstraintValidator implements ConstraintValidator<DateNotInTheFuture, LocalDate> {

    @Override
    public void initialize(DateNotInTheFuture dateNotInTheFuture) {
        // NO-OP
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext cxt) {
        // Another validator should check for this if required
        if (localDate == null) {
            return true;
        }

        return !localDate.isAfter(LocalDate.now());
    }
}
