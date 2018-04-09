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
 * Validates the Interest.
 */
public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {

    private static final String MAY_NOT_BE_NULL_OR_EMPTY = "may not be null or empty";

    @Override
    public void initialize(ValidInterest constraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(Interest interest, ConstraintValidatorContext validatorContext) {
        if (interest == null || (interest.getType() != null
            && interest.getType().equals(Interest.InterestType.NO_INTEREST))) {
            return true;
        } else if (interest != null && interest.getType() == null) {
            setValidationErrors(validatorContext, "type", MAY_NOT_BE_NULL_OR_EMPTY);
            return false;
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
            setValidationErrors(validatorContext, "interestDate", MAY_NOT_BE_NULL_OR_EMPTY);
            return false;
        }
        return validateInterestDate(interestDate, validatorContext);
    }

    private boolean validateDifferentInterestCriteria(Interest interest,
                                                      ConstraintValidatorContext validatorContext) {
        BigDecimal rate = interest.getRate();
        String reason = interest.getReason();
        final Function<String, Boolean> checkReason = reason1 -> reason1 == null || reason1.isEmpty();
        if (rate == null || checkReason.apply(reason)) {
            setValidationErrors(validatorContext, "rate or reason", MAY_NOT_BE_NULL_OR_EMPTY);
            return false;
        } else if (rate.compareTo(BigDecimal.ZERO) != 1) {
            setValidationErrors(validatorContext, "rate", "has to be greater than zero value");
            return false;
        }

        return validateInterestDate(interest.getInterestDate(), validatorContext);
    }

    private boolean validateInterestDate(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        InterestDate.InterestDateType interestDateType = interestDate.getType();
        if (interestDateType != null && interestDateType.equals(InterestDate.InterestDateType.CUSTOM)) {
            if (interestDate.getDate() == null || interestDate.getReason().isEmpty()) {
                setValidationErrors(validatorContext,
                    "interestDate.Date or interest.interestDate.reason", MAY_NOT_BE_NULL_OR_EMPTY);
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
