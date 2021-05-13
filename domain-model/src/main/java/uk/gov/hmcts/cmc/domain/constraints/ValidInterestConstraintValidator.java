package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;

public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {

    private static final String NO_INTEREST = "no interest";
    private static final String REASON = "reason";
    private static final String INTEREST_DATE = "interestDate";
    private static final String INTEREST_BREAKDOWN = "interestBreakdown";
    private static final String DAILY_AMOUNT = "specificDailyAmount";
    private static final String STANDARD = "standard";
    private static final String DIFFERENT = "different";
    private static final String BREAKDOWN = "breakdown";

    @Override
    public boolean isValid(Interest interest, ConstraintValidatorContext validatorContext) {
        if (interest == null || interest.getType() == null) {
            return true;
        }

        switch (interest.getType()) {
            case NO_INTEREST:
                return validateNoInterest(interest, validatorContext);
            case STANDARD:
                return validateStandardInterest(interest, validatorContext);
            case DIFFERENT:
                return validateDifferentInterest(interest, validatorContext);
            case BREAKDOWN:
                return validateBreakdownInterest(interest, validatorContext);
            default:
                return true;
        }
    }

    private boolean validateNoInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;

        if (interest.getRate() != null) {
            setValidationErrors(validatorContext, "rate", mayNotBeProvidedErrorForType(NO_INTEREST));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, REASON, mayNotBeProvidedErrorForType(NO_INTEREST));
            valid = false;
        }
        if (interest.getInterestDate() != null) {
            setValidationErrors(validatorContext, INTEREST_DATE,
                mayNotBeProvidedErrorForType(NO_INTEREST));
            valid = false;
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(NO_INTEREST));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, DAILY_AMOUNT,
                mayNotBeProvidedErrorForType(NO_INTEREST));
            valid = false;
        }
        return valid;
    }

    private boolean validateStandardInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType(STANDARD));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, REASON, mayNotBeProvidedErrorForType(STANDARD));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, INTEREST_DATE, mayNotBeNullErrorForType(STANDARD));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, INTEREST_DATE);
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(STANDARD));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, DAILY_AMOUNT, mayNotBeProvidedErrorForType(STANDARD));
            valid = false;
        }
        return valid;
    }

    private boolean validateDifferentInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType(DIFFERENT));
            valid = false;
        }
        if (interest.getReason() == null) {
            setValidationErrors(validatorContext, REASON, mayNotBeNullErrorForType(DIFFERENT));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, INTEREST_DATE, mayNotBeNullErrorForType(DIFFERENT));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, INTEREST_DATE);
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(DIFFERENT));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, DAILY_AMOUNT, mayNotBeProvidedErrorForType(DIFFERENT));
        }
        return valid;
    }

    private boolean validateInterestReason(String reason, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        if (reason != null) {
            valid = setValidationErrors(validatorContext, REASON,
                mayNotBeProvidedErrorForType(BREAKDOWN));
        }
        return valid;
    }

    private boolean validateInterestBreakdown(InterestBreakdown interestBreakdown, ConstraintValidatorContext
        validatorContext, boolean valid) {
        boolean validError;
        if (interestBreakdown == null) {
            validError = setValidationErrors(validatorContext, INTEREST_BREAKDOWN,
                mayNotBeNullErrorForType(BREAKDOWN));
        } else {
            validError = valid && validateField(validatorContext, interestBreakdown, INTEREST_BREAKDOWN);
        }
        return valid ? validError : valid;
    }

    private boolean validateInterestDateNull(Interest interest, ConstraintValidatorContext context, boolean valid) {
        boolean validError = true;
        if ((interest.getRate() == null) && interest.getSpecificDailyAmount().isEmpty()) {
            setValidationErrors(context,
                "rate",
                mayNotBeNullErrorForType(BREAKDOWN));
            validError = setValidationErrors(context,
                DAILY_AMOUNT,
                mayNotBeNullErrorForType(BREAKDOWN));
        }
        return valid ? validError : valid;
    }

    private boolean validateInterestDateNotNull(Interest interest, ConstraintValidatorContext context, boolean valid) {
        boolean validError = true;
        if ((interest.getRate() != null) || interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(context,
                "rate",
                mayNotBeProvidedErrorForType(BREAKDOWN));
            validError = setValidationErrors(context,
                DAILY_AMOUNT,
                mayNotBeProvidedErrorForType(BREAKDOWN));
        }
        return valid ? validError : valid;
    }

    private boolean validateBreakdownInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid;
        InterestDate interestDate = interest.getInterestDate();
        InterestBreakdown interestBreakdown = interest.getInterestBreakdown();

        valid = validateInterestReason(interest.getReason(), validatorContext);
        valid = validateInterestBreakdown(interestBreakdown, validatorContext, valid);

        if (interestDate == null) {
            valid = setValidationErrors(validatorContext, INTEREST_DATE,
                mayNotBeNullErrorForType(BREAKDOWN));
        } else {
            switch (interestDate.getEndDateType()) {
                case SETTLED_OR_JUDGMENT:
                    validateInterestDateNull(interest, validatorContext, valid);
                    break;
                case SUBMISSION:
                    validateInterestDateNotNull(interest, validatorContext, valid);
                    break;
                default:
                    valid = false;
            }
            valid = valid && validateField(validatorContext, interestDate, INTEREST_DATE);
        }
        return valid;
    }

    private boolean setValidationErrors(ConstraintValidatorContext context, String fieldName, String... errors) {
        context.disableDefaultConstraintViolation();
        for (String error : errors) {
            context.buildConstraintViolationWithTemplate(error)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        }
        return false;
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

    private String mayNotBeNullErrorForType(String type) {
        return String.format("may not be null when interest type is '%s'", type);
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when interest type is '%s'", type);
    }

}
