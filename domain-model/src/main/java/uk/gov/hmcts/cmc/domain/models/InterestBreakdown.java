package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class InterestBreakdown {
    @NotNull
    @Money
    @DecimalMin(value = "0.00")
    private final BigDecimal totalAmount;

    @NotBlank
    private final String explanation;

    public InterestBreakdown(BigDecimal totalAmount, String explanation) {
        this.totalAmount = totalAmount;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
