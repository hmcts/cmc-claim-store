package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class CourtOrder {

    private final String details;
    private final BigDecimal amountOwed;
    private final BigDecimal monthlyInstalmentAmount;

    public CourtOrder(String details, BigDecimal amountOwed, BigDecimal monthlyInstalmentAmount) {
        this.details = details;
        this.amountOwed = amountOwed;
        this.monthlyInstalmentAmount = monthlyInstalmentAmount;
    }

    public String getDetails() {
        return details;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public BigDecimal getMonthlyInstalmentAmount() {
        return monthlyInstalmentAmount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CourtOrder that = (CourtOrder) other;
        return Objects.equals(details, that.details)
            && Objects.equals(amountOwed, that.amountOwed)
            && Objects.equals(monthlyInstalmentAmount, that.monthlyInstalmentAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, amountOwed, monthlyInstalmentAmount);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
