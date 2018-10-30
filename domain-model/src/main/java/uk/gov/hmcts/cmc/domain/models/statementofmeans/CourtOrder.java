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
public class CourtOrder {

    @NotBlank
    private final String claimNumber;

    @NotNull
    @Money
    @DecimalMin(value = "1.00")
    private final BigDecimal amountOwed;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal monthlyInstalmentAmount;

    public CourtOrder(String claimNumber, BigDecimal amountOwed, BigDecimal monthlyInstalmentAmount) {
        this.claimNumber = claimNumber;
        this.amountOwed = amountOwed;
        this.monthlyInstalmentAmount = monthlyInstalmentAmount;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public BigDecimal getMonthlyInstalmentAmount() {
        return monthlyInstalmentAmount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
