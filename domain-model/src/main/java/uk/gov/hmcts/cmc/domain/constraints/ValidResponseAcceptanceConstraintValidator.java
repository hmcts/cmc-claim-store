package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidResponseAcceptanceConstraintValidator
    implements ConstraintValidator<ValidResponseAcceptance, ResponseAcceptation> {

    static class Fields {
        static final String COURT_DETERMINATION = "courtDetermination";
        static final String CLAIMANT_PAYMENT_INTENTION = "claimantPaymentIntention";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(ResponseAcceptation responseAcceptation, ConstraintValidatorContext context) {
        if (responseAcceptation == null) {
            return true;
        }

        if (responseAcceptation.getClaimantPaymentIntention().isPresent()) {
            if (!responseAcceptation.getCourtDetermination().isPresent()) {
                setValidationErrors(context, Fields.COURT_DETERMINATION,
                    "is mandatory when ".concat(Fields.CLAIMANT_PAYMENT_INTENTION).concat(" is present"));
                return false;
            }
        }

        if (responseAcceptation.getCourtDetermination().isPresent()) {
            if (!responseAcceptation.getClaimantPaymentIntention().isPresent()) {
                setValidationErrors(context, Fields.CLAIMANT_PAYMENT_INTENTION,
                    "is mandatory when ".concat(Fields.COURT_DETERMINATION).concat(" is present"));
                return false;
            }
        }

        return true;
    }
}
