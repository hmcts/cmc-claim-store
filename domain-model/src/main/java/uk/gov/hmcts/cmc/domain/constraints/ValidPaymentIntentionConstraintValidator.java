package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidPaymentIntention, PaymentIntention> {

    @Override
    public boolean isValid(PaymentIntention instance, ConstraintValidatorContext context) {
        if (instance == null) {
            return true;
        }

        return PaymentValidator.isValid(
            context,
            instance.getPaymentOption(),
            instance.getPaymentDate().isPresent(),
            instance.getRepaymentPlan().isPresent()
        );
    }
}
