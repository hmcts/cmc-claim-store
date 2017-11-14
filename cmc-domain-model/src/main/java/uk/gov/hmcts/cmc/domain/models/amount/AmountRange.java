package uk.gov.hmcts.cmc.domain.models.amount;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.constraints.Money;
import uk.gov.hmcts.cmc.domain.utils.ToStringStyle;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class AmountRange implements Amount {

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal lowerValue;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal higherValue;

    public AmountRange(final BigDecimal lowerValue, final BigDecimal higherValue) {
        this.lowerValue = lowerValue;
        this.higherValue = higherValue;
    }

    public Optional<BigDecimal> getLowerValue() {
        return Optional.ofNullable(lowerValue);
    }

    public BigDecimal getHigherValue() {
        return higherValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AmountRange that = (AmountRange) obj;
        return Objects.equals(lowerValue, that.lowerValue)
            && Objects.equals(higherValue, that.higherValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerValue, higherValue);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.ourStyle());

    }
}
