package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidCourtDeterminationConstraintValidator
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
        if (courtDetermination == null) {
            return true;
        }

        BigDecimal disposableIncome = courtDetermination.getDisposableIncome();

        if (disposableIncome == null) {
            setValidationErrors(context, Fields.DISPOSABLE_INCOME, "may not be null");
            return false;
        }

        if (disposableIncome.compareTo(BigDecimal.ZERO) < 0) {
            setValidationErrors(context, Fields.DISPOSABLE_INCOME, "should not be less than 0");
            return false;
        }

        DecisionType decisionType = courtDetermination.getDecisionType();
        if (DecisionType.DEFENDANT != decisionType && disposableIncome.compareTo(BigDecimal.ZERO) == 0) {
            setValidationErrors(context, Fields.DISPOSABLE_INCOME,
                "should be greater than 0 for " + decisionType.name() + (" decision type"));
            return false;
        }

        return true;
    }
}
