package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.InterestDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidInterestDateConstraintValidator implements ConstraintValidator<ValidInterestDate, InterestDate> {

    public static class Fields {
        public static final String REASON = "reason";
    }

    @Override
    public boolean isValid(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        if (interestDate == null) {
            return true;
        }

        if (interestDate.getType() == null) {
            return validateNullType(interestDate, validatorContext);
        }

        switch (interestDate.getType()) {
            case CUSTOM:
                return validateCustomType(interestDate, validatorContext);
            case SUBMISSION:
                return validateSubmissionType(interestDate, validatorContext);
            default:
                return true;
        }
    }

    private boolean validateCustomType(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        boolean valid = true;

        if (interestDate.getDate() == null) {
            setValidationErrors(validatorContext, "date", "may not be null");
            valid = false;
        }
        if (interestDate.getReason() == null) {
            setValidationErrors(validatorContext, Fields.REASON, "may not be null");
            valid = false;
        }
        return valid;
    }

    private boolean validateSubmissionType(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        boolean valid = true;

        if (interestDate.getDate() != null) {
            setValidationErrors(validatorContext, "date", "may not be provided when type is 'submission'");
            valid = false;
        }
        if (interestDate.getReason() != null) {
            setValidationErrors(validatorContext, Fields.REASON, "may not be provided when type is 'submission'");
            valid = false;
        }
        return valid;
    }

    private boolean validateNullType(InterestDate interestDate, ConstraintValidatorContext validatorContext) {
        boolean valid = true;

        if (interestDate.getDate() != null) {
            setValidationErrors(validatorContext, "date", "may not be provided when type is undefined");
            valid = false;
        }
        if (interestDate.getReason() != null) {
            setValidationErrors(validatorContext, Fields.REASON, "may not be provided when type is undefined");
            valid = false;
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
}
