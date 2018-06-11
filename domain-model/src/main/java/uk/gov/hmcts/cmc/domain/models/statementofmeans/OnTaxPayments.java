package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class OnTaxPayments {

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amountYouOwe;

    @NotBlank
    private final String reason;

    public OnTaxPayments(BigDecimal amountYouOwe, String reason) {
        this.amountYouOwe = amountYouOwe;
        this.reason = reason;
    }

    public BigDecimal getAmountYouOwe() {
        return amountYouOwe;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        OnTaxPayments that = (OnTaxPayments) other;
        return Objects.equals(amountYouOwe, that.amountYouOwe)
            && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountYouOwe, reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
