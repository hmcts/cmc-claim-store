package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;

/**
 * Intended to replace {@link InterDependentFieldsConstraintValidator}.
 * Validates the BREAKDOWN interest type only for now.
 */
public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {

    private static final String notProvidedMessage = "is not provided";

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
                return validateDifferentInterestCriteria(interest, validatorContext);
            case STANDARD:
                return validateStandardRate(interest.getInterestDate(), validatorContext);
            default:
                return true;
        }
    }

    private boolean validateStandardRate(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        if (interestDate == null) {
            setValidationErrors(validatorContext, "interestDate", notProvidedMessage);
            return false;
        }
        return true;
    }

    private boolean validateDifferentInterestCriteria(Interest interest,
                                                      ConstraintValidatorContext validatorContext) {
        BigDecimal rate = interest.getRate();
        String reason = interest.getReason();
        boolean flag = true;
        if (rate == null) {
            setValidationErrors(validatorContext, "rate", notProvidedMessage);
            return false;
        } else {
            final Function<String, Boolean> checkReason = reason1 -> reason1 == null || reason1.isEmpty();
            if (checkReason.apply(reason)) {
                setValidationErrors(validatorContext, "reason", notProvidedMessage);
                return false;
            } else if (rate.compareTo(BigDecimal.ZERO) != 1 && !checkReason.apply(reason)) {
                setValidationErrors(validatorContext, "rate", "has to be greater than zero value");
                return false;
            }
            flag = validateInterestDate(interest.getInterestDate(), validatorContext);
        }
        return flag;
    }

    private boolean validateInterestDate(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        if (interestDate.getType().equals(InterestDate.InterestDateType.CUSTOM)) {
            if (interestDate.getDate() == null || interestDate.getReason().isEmpty()) {
                setValidationErrors(validatorContext, "interestDate.Date, interestDate.reason", notProvidedMessage);
                return false;
            }
        }
        return true;
    }

    private boolean validateBreakdownInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        InterestBreakdown interestBreakdown = interest.getInterestBreakdown();
        if (interestBreakdown == null) {
            setValidationErrors(validatorContext, "interestBreakdown", "may not be null");
            return false;
        } else {
            return validateEitherInterestRateOrSpecificAmountIsPresent(interest, validatorContext)
                && validateField(validatorContext, interestBreakdown, "interestBreakdown");
        }
    }

    private boolean validateEitherInterestRateOrSpecificAmountIsPresent(
        Interest interest,
        ConstraintValidatorContext validatorContext) {

        if (interest.getSpecificDailyAmount().isPresent() && interest.getRate() != null) {
            setValidationErrors(
                validatorContext,
                "rate",
                "either rate or specific amount should be claimed");
            return false;
        }
        return true;
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
