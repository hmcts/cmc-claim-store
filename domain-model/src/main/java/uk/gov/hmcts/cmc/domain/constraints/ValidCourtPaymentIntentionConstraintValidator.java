package uk.gov.hmcts.cmc.domain.constraints;

import org.apache.commons.lang3.BooleanUtils;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidCourtPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidCourtDetermination, CourtDetermination> {

    static class Fields {
        static final String DISPOSABLE_INCOME = "disposableIncome";
        static final String COURT_PAYMENT_INTENTION = "courtPaymentIntention";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(CourtDetermination courtDetermination,
                           ConstraintValidatorContext context) {
        if (courtDetermination == null) {
            return true;
        }

        if (courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) < 0) {
            setValidationErrors(context, Fields.DISPOSABLE_INCOME, "should not be less than 0");
            return false;
        }

        if (DecisionType.CLAIMANT_IN_FAVOUR_OF_DEFENDANT == courtDetermination.getDecisionType()) {
            if (courtDetermination.getCourtPaymentIntention().isPresent()) {
                setValidationErrors(context, Fields.COURT_PAYMENT_INTENTION, "should be blank for decision " + DecisionType.CLAIMANT_IN_FAVOUR_OF_DEFENDANT.name());
                return false;

            } else {
                return true;
            }
        }

        boolean[] booleans = new boolean[2];
        booleans[0] = (courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) == 0);
        booleans[1] = courtDetermination.getCourtPaymentIntention().isPresent();

        return BooleanUtils.xor(booleans);
    }
}
