package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.ClaimantAmount;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ClaimantAmount
@EqualsAndHashCode
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
