package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.AdmissionResponseValidator.hasValidPaymentPlanDetails;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    @Override
    public boolean isValid(PartAdmissionResponse partAdmissionResponse, ConstraintValidatorContext context) {

        if (partAdmissionResponse == null) {
            return true;
        }

        return partAdmissionResponse.getPaymentOption()
            .map(paymentOption -> hasValidPaymentPlanDetails(
                context,
                partAdmissionResponse.getPaymentDate().isPresent(),
                partAdmissionResponse.getRepaymentPlan().isPresent(),
                paymentOption
                )
            )
            .orElse(true);
    }
}
