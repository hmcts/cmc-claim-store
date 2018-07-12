package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;

import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.AdmissionResponseValidator.Fields.PAYMENT_DATE;
import static uk.gov.hmcts.cmc.domain.constraints.AdmissionResponseValidator.Fields.REPAYMENT_PLAN;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class AdmissionResponseValidator {

    private AdmissionResponseValidator() {
    }

    public static class Fields {
        public static final String PAYMENT_DATE = "paymentDate";
        public static final String REPAYMENT_PLAN = "repaymentPlan";
    }

    public static boolean hasValidPaymentPlanDetails(
        ConstraintValidatorContext context,
        boolean hasPaymentDate,
        boolean hasRepaymentPlan,
        PaymentOption paymentOption
    ) {
        switch (paymentOption) {
            case IMMEDIATELY:
                return validateImmediately(hasPaymentDate, hasRepaymentPlan, context);
            case BY_SPECIFIED_DATE:
                return validateBySetDate(hasPaymentDate, hasRepaymentPlan, context);
            case INSTALMENTS:
                return validateInstalments(hasPaymentDate, hasRepaymentPlan, context);
            default:
                return false;
        }
    }

    private static boolean validateImmediately(
        boolean hasPaymentDate,
        boolean hasRepaymentPlan,
        ConstraintValidatorContext context
    ) {
        boolean valid = true;
        String immediately = IMMEDIATELY.getDescription();

        if (!hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeProvidedError("paymentOption", immediately));
            valid = false;
        }

        if (hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeProvidedError("paymentOption", immediately));
            valid = false;
        }

        return valid;
    }

    private static boolean validateBySetDate(
        boolean hasPaymentDate,
        boolean hasRepaymentPlan,
        ConstraintValidatorContext context
    ) {
        boolean valid = true;
        String bySetDate = BY_SPECIFIED_DATE.getDescription();

        if (!hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeProvidedError("paymentOption", bySetDate));
            valid = false;
        }

        if (hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeProvidedError("paymentOption", bySetDate));
            valid = false;
        }

        return valid;
    }

    private static boolean validateInstalments(
        boolean hasPaymentDate,
        boolean hasRepaymentPlan,
        ConstraintValidatorContext context
    ) {
        boolean valid = true;
        String instalments = INSTALMENTS.getDescription();

        if (hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeProvidedError("paymentOption", instalments));
            valid = false;
        }

        if (!hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeProvidedError("paymentOption", instalments));
            valid = false;
        }

        return valid;
    }

}
