package uk.gov.hmcts.cmc.domain.models.amount;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class AmountRange implements Amount {

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal lowerValue;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal higherValue;

    @Builder
    public AmountRange(BigDecimal lowerValue, BigDecimal higherValue) {
        this.lowerValue = lowerValue;
        this.higherValue = higherValue;
    }

    public Optional<BigDecimal> getLowerValue() {
        return Optional.ofNullable(lowerValue);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());

    }
}
