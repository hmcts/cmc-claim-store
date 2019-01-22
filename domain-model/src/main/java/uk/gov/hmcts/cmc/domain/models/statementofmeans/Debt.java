package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class Debt extends CollectionId {

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

    @Builder
    public Debt(String id, String description, BigDecimal totalOwed, BigDecimal monthlyPayments) {
        super(id);
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
