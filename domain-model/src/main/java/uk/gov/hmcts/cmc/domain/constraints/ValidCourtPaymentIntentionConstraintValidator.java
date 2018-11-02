package uk.gov.hmcts.cmc.domain.constraints;

import org.apache.commons.lang3.BooleanUtils;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidCourtPaymentIntentionConstraintValidator
    implements ConstraintValidator<ValidCourtDetermination, CourtDetermination> {

    static class Fields {
        static final String DISPOSABLE_INCOME = "disposableIncome";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(CourtDetermination courtDetermination,
                           ConstraintValidatorContext context) {
        if (courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) < 0) {
            setValidationErrors(context, Fields.DISPOSABLE_INCOME, "should not be less than 0");
            return false;
        }

        return BooleanUtils.xor(new boolean[]{courtDetermination.getDisposableIncome().compareTo(BigDecimal.ZERO) == 0,
            courtDetermination.getCourtPaymentIntention().isPresent()});
    }
}
