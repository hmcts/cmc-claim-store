package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.FULL_BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class ValidAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, FullAdmissionResponse> {

    public static class Fields {
        public static final String PAYMENT_DATE = "paymentDate";
        public static final String REPAYMENT_PLAN = "repaymentPlan";
    }

    @Override
    public boolean isValid(FullAdmissionResponse fullAdmissionResponse, ConstraintValidatorContext context) {
        if (fullAdmissionResponse == null) {
            return true;
        }

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

        if (!value.getPaymentDate().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeNullError("paymentOption", immediately));
            valid = false;
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeProvidedError("paymentOption", immediately));
            valid = false;
        }

        return valid;
    }

    private boolean validateBySetDate(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String bySetDate = FULL_BY_SPECIFIED_DATE.getDescription();

        if (!value.getPaymentDate().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeNullError("paymentOption", bySetDate));
            valid = false;
        }

        if (value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeProvidedError("paymentOption", bySetDate));
            valid = false;
        }

        return valid;
    }

    private boolean validateInstalments(FullAdmissionResponse value, ConstraintValidatorContext context) {
        boolean valid = true;
        String instalments = INSTALMENTS.getDescription();

        if (value.getPaymentDate().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DATE, mayNotBeProvidedError("paymentOption", instalments));
            valid = false;
        }

        if (!value.getRepaymentPlan().isPresent()) {
            setValidationErrors(context, Fields.REPAYMENT_PLAN, mayNotBeNullError("paymentOption", instalments));
            valid = false;
        }

        return valid;
    }
}
