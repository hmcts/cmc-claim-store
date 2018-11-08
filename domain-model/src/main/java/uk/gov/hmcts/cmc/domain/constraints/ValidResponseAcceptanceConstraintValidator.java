package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Optional;
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

        Optional<CourtDetermination> courtDetermination = responseAcceptation.getCourtDetermination();
        Optional<PaymentIntention> claimantPaymentIntention = responseAcceptation.getClaimantPaymentIntention();

        if (claimantPaymentIntention.isPresent() && !courtDetermination.isPresent()) {
            setValidationErrors(context, Fields.COURT_DETERMINATION,
                "is mandatory when " + Fields.CLAIMANT_PAYMENT_INTENTION + " is present");
            return false;
        }

        if (courtDetermination.isPresent() && !claimantPaymentIntention.isPresent()) {
            setValidationErrors(context, Fields.CLAIMANT_PAYMENT_INTENTION,
                "is mandatory when " + Fields.COURT_DETERMINATION + " is present");
            return false;
        }

        return true;
    }
}
