package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.hmcts.cmc.domain.models.constraints.ClaimantAmount;
import uk.gov.hmcts.cmc.domain.models.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ClaimantAmount
public class AmountRow {
    private final String reason;

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    public AmountRow(final String reason, final BigDecimal amount) {
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
}
