package uk.gov.hmcts.cmc.domain.constraints;

import java.time.LocalDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FutureDateConstraintValidator implements ConstraintValidator<FutureDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || value.isAfter(LocalDate.now());
    }

}
