package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.FULL_BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class ValidFullAdmissionConstraintValidator
    implements ConstraintValidator<ValidFullAdmission, FullAdmissionResponse> {

    @Override
    public boolean isValid(FullAdmissionResponse fullAdmissionResponse, ConstraintValidatorContext context) {
        switch (fullAdmissionResponse.getPaymentOption()) {
            case IMMEDIATELY:
                return validateImmediately(fullAdmissionResponse, context);
            case FULL_BY_SPECIFIED_DATE:
                return validateBySetDate(fullAdmissionResponse, context);
            case INSTALMENTS:
                return validateInstalments(fullAdmissionResponse, context);
            default:
                return false;
        }
    }

    private boolean validateImmediately(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String immediately = IMMEDIATELY.getDescription();

        if (value.getPaymentDate().isPresent()) {
            setValidationErrors(context, "paymentDate", mayNotBeProvidedErrorForType(immediately));
            valid = false;
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, "repaymentPlan", mayNotBeProvidedErrorForType(immediately));
            valid = false;
        }

        return valid;
    }

    private boolean validateBySetDate(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String bySetDate = FULL_BY_SPECIFIED_DATE.getDescription();

        if (!value.getPaymentDate().isPresent()) {
            setValidationErrors(context, "paymentDate", mayNotBeNullErrorForType(bySetDate));
            valid = false;
        } else {
            valid = validateField(context, value.getPaymentDate().get(), "paymentDate");
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, "repaymentPlan", mayNotBeProvidedErrorForType(bySetDate));
            valid = false;
        }

        return valid;
    }

    private boolean validateInstalments(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String instalments = INSTALMENTS.getDescription();

        if (value.getPaymentDate().isPresent()) {
            setValidationErrors(context, "paymentDate", mayNotBeProvidedErrorForType(instalments));
            valid = false;
        }

        if (!value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, "repaymentPlan", mayNotBeNullErrorForType(instalments));
            valid = false;
        } else {
            valid = valid && validateField(context, value.getRepaymentPlan().get(), "repaymentPlan");
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
        return String.format("may not be null when payment plan is '%s'", type);
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when payment plan is '%s'", type);
    }

}
