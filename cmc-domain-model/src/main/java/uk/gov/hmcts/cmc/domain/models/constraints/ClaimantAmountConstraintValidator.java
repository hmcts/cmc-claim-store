package uk.gov.hmcts.cmc.domain.models.constraints;

import uk.gov.hmcts.cmc.domain.models.AmountRow;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ClaimantAmountConstraintValidator implements ConstraintValidator<ClaimantAmount, AmountRow> {

    @Override
    public void initialize(ClaimantAmount phone) {
        // NO-OP
    }

    @Override
    public boolean isValid(AmountRow amount, ConstraintValidatorContext cxt) {
        if (amount == null) {
            return true;
        }

        return !((amount.getAmount() == null && amount.getReason() != null)
            || (amount.getAmount() != null && amount.getReason() == null));
    }
}
