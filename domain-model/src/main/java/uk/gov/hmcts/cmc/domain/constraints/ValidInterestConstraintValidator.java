package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;

public class ValidInterestConstraintValidator implements ConstraintValidator<ValidInterest, Interest> {

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
            setValidationErrors(validatorContext, "rate", mayNotBeProvidedErrorForType("no interest"));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, "reason", mayNotBeProvidedErrorForType("no interest"));
            valid = false;
        }
        if (interest.getInterestDate() != null) {
            setValidationErrors(validatorContext, "interestDate", mayNotBeProvidedErrorForType("no interest"));
            valid = false;
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, "interestBreakdown", mayNotBeProvidedErrorForType("no interest"));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, "specificDailyAmount", mayNotBeProvidedErrorForType("no interest"));
            valid = false;
        }
        return valid;
    }

    private boolean validateStandardInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType("standard"));
            valid = false;
        }
        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, "reason", mayNotBeProvidedErrorForType("standard"));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, "interestDate", mayNotBeNullErrorForType("standard"));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, "interestDate");
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, "interestBreakdown", mayNotBeProvidedErrorForType("standard"));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, "specificDailyAmount", mayNotBeProvidedErrorForType("standard"));
            valid = false;
        }
        return valid;
    }

    private boolean validateDifferentInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();

        if (interest.getRate() == null) {
            setValidationErrors(validatorContext, "rate", mayNotBeNullErrorForType("different"));
            valid = false;
        }
        if (interest.getReason() == null) {
            setValidationErrors(validatorContext, "reason", mayNotBeNullErrorForType("different"));
            valid = false;
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, "interestDate", mayNotBeNullErrorForType("different"));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestDate, "interestDate");
        }
        if (interest.getInterestBreakdown() != null) {
            setValidationErrors(validatorContext, "interestBreakdown", mayNotBeProvidedErrorForType("different"));
            valid = false;
        }
        if (interest.getSpecificDailyAmount().isPresent()) {
            setValidationErrors(validatorContext, "specificDailyAmount", mayNotBeProvidedErrorForType("different"));
            valid = false;
        }
        return valid;
    }

    private boolean validateBreakdownInterest(Interest interest, ConstraintValidatorContext validatorContext) {
        boolean valid = true;
        InterestDate interestDate = interest.getInterestDate();
        InterestBreakdown interestBreakdown = interest.getInterestBreakdown();

        if (interest.getReason() != null) {
            setValidationErrors(validatorContext, "reason", mayNotBeProvidedErrorForType("breakdown"));
            valid = false;
        }
        if (interestBreakdown == null) {
            setValidationErrors(validatorContext, "interestBreakdown", mayNotBeNullErrorForType("breakdown"));
            valid = false;
        } else {
            valid = valid && validateField(validatorContext, interestBreakdown, "interestBreakdown");
        }
        if (interestDate == null) {
            setValidationErrors(validatorContext, "interestDate", mayNotBeNullErrorForType("breakdown"));
            valid = false;
        } else {
            switch (interestDate.getEndDateType()) {
                case SETTLED_OR_JUDGMENT:
                    if ((interest.getRate() == null) && !interest.getSpecificDailyAmount().isPresent()) {
                        setValidationErrors(validatorContext,
                            "rate",
                            mayNotBeNullErrorForType("breakdown"));
                        setValidationErrors(validatorContext,
                            "specificDailyAmount",
                            mayNotBeNullErrorForType("breakdown"));
                        valid = false;
                    }
                    break;
                case SUBMISSION:
                    if ((interest.getRate() != null) || interest.getSpecificDailyAmount().isPresent()) {
                        setValidationErrors(validatorContext,
                            "rate",
                            mayNotBeProvidedErrorForType("breakdown"));
                        setValidationErrors(validatorContext,
                            "specificDailyAmount",
                            mayNotBeProvidedErrorForType("breakdown"));
                        valid = false;
                    }
                    break;
                default:
                    valid = false;
            }
            valid = valid && validateField(validatorContext, interestDate, "interestDate");
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
