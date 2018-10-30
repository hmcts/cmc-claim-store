package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCourtPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidCourtDetermination, CourtDetermination> {
    @Override
    public boolean isValid(CourtDetermination courtDetermination,
                           ConstraintValidatorContext constraintValidatorContext) {
        return DecisionType.DEFENDANT == courtDetermination.getDecisionType()
            && !courtDetermination.getCourtPaymentIntention().isPresent();
    }
}
