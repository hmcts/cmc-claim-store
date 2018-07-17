package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidFullAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, FullAdmissionResponse> {

    @Override
    public boolean isValid(FullAdmissionResponse fullAdmissionResponse, ConstraintValidatorContext context) {
        if (fullAdmissionResponse == null) {
            return true;
        }

        boolean hasPaymentDate = fullAdmissionResponse.getPaymentDate().isPresent();
        boolean hasRepaymentPlan = fullAdmissionResponse.getRepaymentPlan().isPresent();
        PaymentOption paymentOption = fullAdmissionResponse.getPaymentOption();
        return PaymentValidator.isValid(context, paymentOption, hasPaymentDate, hasRepaymentPlan);
    }
}
