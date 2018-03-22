package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        InterestBreakdown other = (InterestBreakdown) object;
        return Objects.equals(totalAmount, other.totalAmount)
            && Objects.equals(explanation, other.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalAmount, explanation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
