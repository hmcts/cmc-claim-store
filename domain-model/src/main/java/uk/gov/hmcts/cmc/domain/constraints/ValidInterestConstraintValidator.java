package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;

public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {
    public static class Fields {
        public static final String NO_INTEREST = "no interest";
        public static final String REASON = "reason";
        public static final String INTEREST_DATE = "interestDate";
        public static final String INTEREST_BREAKDOWN = "interestBreakdown";
        public static final String DAILY_AMOUNT = "specificDailyAmount";
        public static final String STANDARD = "standard";
        public static final String DIFFERENT = "different";
        public static final String BREAKDOWN = "breakdown";
    }

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
            setValidationErrors(validatorContext, "rate", mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, Fields.REASON, mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getInterestDate() != null) {
            setValidationErrors(validatorContext, Fields.INTEREST_DATE,
                mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, Fields.DAILY_AMOUNT,
                mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        return valid;
    }

    private boolean validateStandardInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType(Fields.STANDARD));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, Fields.REASON, mayNotBeProvidedErrorForType(Fields.STANDARD));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, Fields.INTEREST_DATE, mayNotBeNullErrorForType(Fields.STANDARD));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, Fields.INTEREST_DATE);
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(Fields.STANDARD));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, Fields.DAILY_AMOUNT, mayNotBeProvidedErrorForType(Fields.STANDARD));
            valid = false;
        }
        return valid;
    }

    private boolean validateDifferentInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType(Fields.DIFFERENT));
            valid = false;
        }
        if (interest.getReason() == null) {
            setValidationErrors(validatorContext, Fields.REASON, mayNotBeNullErrorForType(Fields.DIFFERENT));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, Fields.INTEREST_DATE, mayNotBeNullErrorForType(Fields.DIFFERENT));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, Fields.INTEREST_DATE);
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN,
                mayNotBeProvidedErrorForType(Fields.DIFFERENT));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, Fields.DAILY_AMOUNT, mayNotBeProvidedErrorForType(Fields.DIFFERENT));
        }
        return valid;
    }

    private boolean validateInterestReason(String reason, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        if (reason != null) {
            valid = setValidationErrors(validatorContext, Fields.REASON,
                mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
        }
        return valid;
    }

    private boolean validateInterestBreakdown(InterestBreakdown interestBreakdown, ConstraintValidatorContext
        validatorContext, boolean valid) {
        boolean validError;
        if (interestBreakdown == null) {
            validError = setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN,
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
        } else {
            validError = valid && validateField(validatorContext, interestBreakdown, Fields.INTEREST_BREAKDOWN);
        }
        return valid ? validError : valid;
    }

    private boolean validateInterestDateNull(Interest interest, ConstraintValidatorContext context, boolean valid) {
        boolean validError = true;
        if ((interest.getRate() == null) && interest.getSpecificDailyAmount().isEmpty()) {
            setValidationErrors(context,
                "rate",
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
            validError = setValidationErrors(context,
                Fields.DAILY_AMOUNT,
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
        }
        return valid ? validError : valid;
    }

    private boolean validateInterestDateNotNull(Interest interest, ConstraintValidatorContext context, boolean valid) {
        boolean validError = true;
        if ((interest.getRate() != null) || interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(context,
                "rate",
                mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
            validError = setValidationErrors(context,
                Fields.DAILY_AMOUNT,
                mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
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
            valid = setValidationErrors(validatorContext, Fields.INTEREST_DATE,
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
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
            valid = valid && validateField(validatorContext, interestDate, Fields.INTEREST_DATE);
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
