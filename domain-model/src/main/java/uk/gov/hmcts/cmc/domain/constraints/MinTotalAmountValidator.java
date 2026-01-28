package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MinTotalAmountValidator implements ConstraintValidator<MinTotalAmount, List<AmountRow>> {

    private BigDecimal minAmount;
    private boolean inclusive;

    @Override
    public void initialize(MinTotalAmount constraintAnnotation) {
        minAmount = new BigDecimal(constraintAnnotation.value());
        inclusive = constraintAnnotation.inclusive();
    }

    @Override
    public boolean isValid(List<AmountRow> rows, ConstraintValidatorContext context) {
        if (rows == null) {
            return true;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (AmountRow row : rows) {
            if (row.getAmount() != null) {
                total = total.add(row.getAmount());
            }
        }
        return inclusive ? isGreaterOrEqualThanMinAmount(total) : isGreaterThanMinAmount(total);
    }

    private boolean isGreaterThanMinAmount(BigDecimal value) {
        return value.compareTo(minAmount) > 0;
    }

    private boolean isGreaterOrEqualThanMinAmount(BigDecimal value) {
        return value.compareTo(minAmount) >= 0;
    }

}
