package uk.gov.hmcts.cmc.domain.constraints;

import java.util.Collection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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
