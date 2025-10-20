package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;

public class EachNotNullConstraintValidator implements ConstraintValidator<EachNotNull, Collection<?>> {
    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        for (Object element : value) {
            if (element == null) {
                return false;
            }
        }

        return true;
    }
}
