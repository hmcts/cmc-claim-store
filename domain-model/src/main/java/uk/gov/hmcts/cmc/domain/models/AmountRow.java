package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.ClaimantAmount;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@ClaimantAmount
@EqualsAndHashCode(callSuper = true)
public class AmountRow extends CollectionId {
    private final String reason;

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    @Builder
    public AmountRow(String id, String reason, BigDecimal amount) {
        super(id);
        this.reason = reason;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
