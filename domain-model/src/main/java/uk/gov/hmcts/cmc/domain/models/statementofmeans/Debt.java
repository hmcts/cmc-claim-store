package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class Debt {

    @NotBlank
    private final String description;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal totalOwed;

    @NotNull
    @Money
    @DecimalMin(value = "0.00")
    private final BigDecimal monthlyPayments;

    public Debt(String description, BigDecimal totalOwed, BigDecimal monthlyPayments) {
        this.description = description;
        this.totalOwed = totalOwed;
        this.monthlyPayments = monthlyPayments;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public BigDecimal getMonthlyPayments() {
        return monthlyPayments;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
