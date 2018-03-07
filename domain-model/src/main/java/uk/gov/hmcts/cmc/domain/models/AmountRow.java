package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.ClaimantAmount;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ClaimantAmount
public class AmountRow {
    private final String reason;

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    public AmountRow(String reason, BigDecimal amount) {
        this.reason = reason;
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AmountRow amountRow = (AmountRow) other;
        return Objects.equals(reason, amountRow.reason)
            && Objects.equals(amount, amountRow.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, amount);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
