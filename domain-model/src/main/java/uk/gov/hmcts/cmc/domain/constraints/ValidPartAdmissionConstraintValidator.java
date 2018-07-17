package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    @Override
    public boolean isValid(PartAdmissionResponse partAdmissionResponse, ConstraintValidatorContext context) {
        if (partAdmissionResponse == null) {
            return true;
        }

        return partAdmissionResponse.getPaymentIntention()
            .map(paymentDeclaration -> PaymentValidator.isValid(
                context,
                paymentDeclaration.getPaymentOption(),
                paymentDeclaration.getPaymentDate().isPresent(),
                paymentDeclaration.getRepaymentPlan().isPresent()
                )
            )
            .orElse(true);
    }
}
