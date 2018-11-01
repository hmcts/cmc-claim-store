package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCourtPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidCourtDetermination, CourtDetermination> {
    @Override
    public boolean isValid(CourtDetermination courtDetermination,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (courtDetermination.getDecisionType() == DecisionType.DEFENDANT
            && courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) <= 0
            && courtDetermination.getCourtPaymentIntention().isPresent()) {
            return false;
        }

        if (courtDetermination.getDecisionType() == DecisionType.DEFENDANT
            && courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) > 0
            && !courtDetermination.getCourtPaymentIntention().isPresent()) {
            return false;
        }

        if (courtDetermination.getDecisionType() != DecisionType.DEFENDANT
            && courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) <= 0
            && courtDetermination.getCourtPaymentIntention().isPresent()) {
            return false;
        }

        if (courtDetermination.getDecisionType() != DecisionType.DEFENDANT
            && courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) > 0
            && !courtDetermination.getCourtPaymentIntention().isPresent()) {
            return false;
        }

        return true;
    }
}
