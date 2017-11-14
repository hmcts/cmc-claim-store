
package uk.gov.hmcts.cmccase.models.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberConstraintValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String normalized = normalize(value);
        return !normalized.isEmpty()
            && doesntStartWithZero(normalized)
            && isWithInLengthLimit(normalized);
    }

    private boolean isWithInLengthLimit(final String normalized) {
        return normalized.length() == 10 || normalized.length() == 9 || normalized.length() == 7;
    }

    private String normalize(String value) {
        return value
            .replaceAll("\\(|\\)| |-|\\+", "")
            .replaceAll("^(00)?44", "")
            .replaceAll("^0", "")
            .replaceAll("[^0-9]", "");
    }

    private boolean doesntStartWithZero(String value) {
        char firstChar = value.charAt(0);
        return firstChar <= '9' && firstChar > '0';
    }

}
