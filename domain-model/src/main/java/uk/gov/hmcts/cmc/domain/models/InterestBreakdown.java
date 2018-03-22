package uk.gov.hmcts.cmc.domain.models;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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

}
