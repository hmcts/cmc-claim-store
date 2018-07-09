package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.AdmissionResponseValidator.hasValidPaymentPlanDetails;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    @Override
    public boolean isValid(PartAdmissionResponse partAdmissionResponse, ConstraintValidatorContext context) {

        if (partAdmissionResponse == null || !partAdmissionResponse.getPaymentOption().isPresent()) {
            return true;
        }

        PaymentOption paymentOption = partAdmissionResponse.getPaymentOption().get();
        boolean hasPaymentDate = partAdmissionResponse.getPaymentDate().isPresent();
        boolean hasRepaymentPlan = partAdmissionResponse.getRepaymentPlan().isPresent();

        return hasValidPaymentPlanDetails(context, hasPaymentDate, hasRepaymentPlan, paymentOption);
    }
}
