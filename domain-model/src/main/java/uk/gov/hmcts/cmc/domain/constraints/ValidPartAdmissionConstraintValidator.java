package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    static class Fields {
        static String PAYMENT_DECLARATION = "paymentDeclaration";
        static String PAYMENT_INTENTION = "paymentIntention";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(PartAdmissionResponse response, ConstraintValidatorContext context) {
        if (response == null) {
            return true;
        }

        boolean valid = true;

        if (response.getPaymentDeclaration().isPresent() && response.getPaymentIntention().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DECLARATION, mayNotBeProvidedError(Fields.PAYMENT_INTENTION));
            setValidationErrors(context, Fields.PAYMENT_INTENTION, mayNotBeProvidedError(Fields.PAYMENT_DECLARATION));
            valid = false;
        } else if (!response.getPaymentDeclaration().isPresent() && !response.getPaymentIntention().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DECLARATION, mayNotBeNullError(Fields.PAYMENT_INTENTION));
            setValidationErrors(context, Fields.PAYMENT_INTENTION, mayNotBeNullError(Fields.PAYMENT_DECLARATION));
            valid = false;
        }

        return valid;
    }
}
