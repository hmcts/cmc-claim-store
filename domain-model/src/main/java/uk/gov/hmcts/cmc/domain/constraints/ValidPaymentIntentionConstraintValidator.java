package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import static uk.gov.hmcts.cmc.domain.constraints.ValidPaymentIntentionConstraintValidator.Fields.PAYMENT_DATE;
import static uk.gov.hmcts.cmc.domain.constraints.ValidPaymentIntentionConstraintValidator.Fields.PAYMENT_OPTION;
import static uk.gov.hmcts.cmc.domain.constraints.ValidPaymentIntentionConstraintValidator.Fields.REPAYMENT_PLAN;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class ValidPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidPaymentIntention, PaymentIntention> {

    static class Fields {
        static final String PAYMENT_OPTION = "paymentOption";
        static final String PAYMENT_DATE = "paymentDate";
        static final String REPAYMENT_PLAN = "repaymentPlan";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(PaymentIntention instance, ConstraintValidatorContext context) {
        if (instance == null) {
            return true;
        }

        boolean hasPaymentDate = instance.getPaymentDate().isPresent();
        boolean hasRepaymentPlan = instance.getRepaymentPlan().isPresent();
        switch (instance.getPaymentOption()) {
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
        String paymentOption = IMMEDIATELY.getDescription();

        if (!hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeNullError(PAYMENT_OPTION, paymentOption));
            valid = false;
        }

        if (hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeProvidedError(PAYMENT_OPTION, paymentOption));
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
        String paymentOption = BY_SPECIFIED_DATE.getDescription();

        if (!hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeNullError(PAYMENT_OPTION, paymentOption));
            valid = false;
        }

        if (hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeProvidedError(PAYMENT_OPTION, paymentOption));
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
        String paymentOption = INSTALMENTS.getDescription();

        if (hasPaymentDate) {
            setValidationErrors(context, PAYMENT_DATE, mayNotBeProvidedError(PAYMENT_OPTION, paymentOption));
            valid = false;
        }

        if (!hasRepaymentPlan) {
            setValidationErrors(context, REPAYMENT_PLAN, mayNotBeNullError(PAYMENT_OPTION, paymentOption));
            valid = false;
        }

        return valid;
    }
}
