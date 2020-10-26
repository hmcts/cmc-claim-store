package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.FileHandler;
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
            setValidationErrors(validatorContext, Fields.INTEREST_DATE, mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN, mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, Fields.DAILY_AMOUNT, mayNotBeProvidedErrorForType(Fields.NO_INTEREST));
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
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN, mayNotBeProvidedErrorForType(Fields.STANDARD));
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
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN, mayNotBeProvidedErrorForType(Fields.DIFFERENT));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, Fields.DAILY_AMOUNT, mayNotBeProvidedErrorForType(Fields.DIFFERENT));
            valid = false;
        }
        return valid;
    }

    private boolean validateInterestReason(String reason, ConstraintValidatorContext validatorContext, boolean valid) {
        if (reason != null) {
            setValidationErrors(validatorContext, Fields.REASON, mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
            valid = false;
        }
        return valid;
    }

    private boolean validateInterestBreakdown(InterestBreakdown interestBreakdown, ConstraintValidatorContext validatorContext,boolean valid) {
        if (interestBreakdown == null) {
            setValidationErrors(validatorContext, Fields.INTEREST_BREAKDOWN, mayNotBeNullErrorForType(Fields.BREAKDOWN));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestBreakdown, Fields.INTEREST_BREAKDOWN);
        }
        return valid;
    }

    private boolean validateInterestDateRateNull(Interest interest, ConstraintValidatorContext validatorContext, boolean valid) {
        if ((interest.getRate() == null) && interest.getSpecificDailyAmount().isEmpty()) {
            setValidationErrors(validatorContext,
                "rate",
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
            setValidationErrors(validatorContext,
                Fields.DAILY_AMOUNT,
                mayNotBeNullErrorForType(Fields.BREAKDOWN));
            valid = false;
        }
        return valid;
    }

    private boolean validateInterestDateRateNotNull(Interest interest, ConstraintValidatorContext validatorContext, boolean valid) {
        if ((interest.getRate() != null) || interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext,
                "rate",
                mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
            setValidationErrors(validatorContext,
                Fields.DAILY_AMOUNT,
                mayNotBeProvidedErrorForType(Fields.BREAKDOWN));
            valid = false;
        }
        return valid;
    }

    private boolean validateBreakdownInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();
        InterestBreakdown interestBreakdown = interest.getInterestBreakdown();

        valid = validateInterestReason(interest.getReason(), validatorContext, valid);
        valid = validateInterestBreakdown(interestBreakdown,validatorContext, valid);

        if (interestDate == null) {
            setValidationErrors(validatorContext, Fields.INTEREST_DATE, mayNotBeNullErrorForType(Fields.BREAKDOWN));
            valid = false;
        } else {
            switch (interestDate.getEndDateType()) {
                case SETTLED_OR_JUDGMENT:
                    valid = validateInterestDateRateNull(interest, validatorContext, valid);
                    break;
                case SUBMISSION:
                    valid = validateInterestDateRateNotNull(interest, validatorContext, valid);
                    break;
                default:
                    valid = false;
            }
            valid = valid && validateField(validatorContext, interestDate, Fields.INTEREST_DATE);
        }
        return valid;
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

    private String mayNotBeNullErrorForType(String type) {
        return String.format("may not be null when interest type is '%s'", type);
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when interest type is '%s'", type);
    }

}
