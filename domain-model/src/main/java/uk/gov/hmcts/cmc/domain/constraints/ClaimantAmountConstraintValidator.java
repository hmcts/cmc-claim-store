package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

public class ClaimantAmountConstraintValidator implements ConstraintValidator<ClaimantAmount, AmountRow> {

    @Override
    public boolean isValid(AmountRow amount, ConstraintValidatorContext cxt) {
        if (amount == null) {
            return true;
        }

        return !((amount.getAmount() == null && amount.getReason() != null)
            || (amount.getAmount() != null && amount.getReason() == null));
    }
}
