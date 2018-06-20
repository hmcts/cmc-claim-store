package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.models.PaymentOption.FULL_BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class ValidFullAdmissionConstraintValidator
    implements ConstraintValidator<ValidFullAdmission, FullAdmissionResponse> {

    public static class Fields {
        static final String PAYMENT_DATE = "paymentDate";
        static final String REPAYMENT_PLAN = "repaymentPlan";
        static final String STATEMENT_OF_MEANS = "statementOfMeans";
    }

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
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeProvidedErrorForType(immediately));
            valid = false;
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeProvidedErrorForType(immediately));
            valid = false;
        }

        if (value.getStatementOfMeans().isPresent()) {
            setValidationErrors(context, Fields.STATEMENT_OF_MEANS, mayNotBeProvidedErrorForType(immediately));
            valid = false;
        }

        return valid;
    }

    private boolean validateBySetDate(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String bySetDate = FULL_BY_SPECIFIED_DATE.getDescription();

        if (!value.getPaymentDate().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeNullErrorForType(bySetDate));
            valid = false;
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeProvidedErrorForType(bySetDate));
            valid = false;
        }

        if (!value.getStatementOfMeans().isPresent()) {
            setValidationErrors(context, Fields.STATEMENT_OF_MEANS, mayNotBeNullErrorForType(bySetDate));
            valid = false;
        }

        return valid;
    }

    private boolean validateInstalments(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String instalments = INSTALMENTS.getDescription();

        if (value.getPaymentDate().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeProvidedErrorForType(instalments));
            valid = false;
        }

        if (!value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeNullErrorForType(instalments));
            valid = false;
        }

        if (!value.getStatementOfMeans().isPresent()) {
            setValidationErrors(context, Fields.STATEMENT_OF_MEANS, mayNotBeNullErrorForType(instalments));
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

    private String mayNotBeNullErrorForType(String type) {
        return String.format("may not be null when payment plan is '%s'", type);
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when payment plan is '%s'", type);
    }
}
