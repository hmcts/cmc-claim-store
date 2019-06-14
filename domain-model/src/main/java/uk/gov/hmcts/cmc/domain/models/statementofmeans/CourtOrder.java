package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class CourtOrder extends CollectionId {

    @NotBlank
    private final String claimNumber;

    @NotNull
    @Money
    @DecimalMin(value = "1.00")
    private final BigDecimal amountOwed;

    @NotNull
    @Money
    @DecimalMin(value = "0.00")
    private final BigDecimal monthlyInstalmentAmount;

    @Builder
    public CourtOrder(String id, String claimNumber, BigDecimal amountOwed, BigDecimal monthlyInstalmentAmount) {
        super(id);
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
