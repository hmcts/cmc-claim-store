package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.math.BigDecimal;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;

/**
 * Intended to replace {@link InterDependentFieldsConstraintValidator}.
 * Validates the BREAKDOWN interest type only for now.
 */
public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {
    @Override
    public void initialize(ValidInterest constraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(Interest interest, ConstraintValidatorContext validatorContext) {
        if (interest == null || interest.getType() == null) {
            return true;
        }

        switch (interest.getType()) {
            case BREAKDOWN:
                return validateBreakdownInterest(interest, validatorContext);
            case DIFFERENT:
                return validateDifferentInterestCriteria(interest.getRate(), interest.getReason());
            default:
                return true;
        }
    }

    private boolean validateDifferentInterestCriteria(BigDecimal rate, String reason) {
        if (rate == null || (reason == null || reason.isEmpty())) {
            return false;
        } else if (rate.compareTo(BigDecimal.ZERO) != 1) {
            return false;
        }
        return true;
    }

    private boolean validateBreakdownInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        InterestBreakdown interestBreakdown = interest.getInterestBreakdown();
        if (interestBreakdown == null) {
            setValidationErrors(validatorContext, "interestBreakdown", "may not be null");
            return false;
        } else {
            return validateField(validatorContext, interestBreakdown, "interestBreakdown");
        }
    }

    private void setValidationErrors(ConstraintValidatorContext validatorContext, String fieldName, String... errors) {
        validatorContext.disableDefaultConstraintViolation();
        for (String error : errors) {
            validatorContext.buildConstraintViolationWithTemplate(error)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        }
    }

    private boolean validateField(ConstraintValidatorContext validatorContext, Object field, String fieldName) {
        Set<String> validationErrors = validate(field);
        if (!validationErrors.isEmpty()) {
            setValidationErrors(validatorContext, fieldName, toArray(validationErrors));
            return false;
        }
        return true;
    }

    private String[] toArray(Set<String> set) {
        return set.toArray(new String[]{});
    }

}
