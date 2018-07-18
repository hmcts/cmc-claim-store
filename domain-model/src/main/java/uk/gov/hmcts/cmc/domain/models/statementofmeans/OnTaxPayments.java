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
public class OnTaxPayments {

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amountYouOwe;

    @NotBlank
    private final String reason;

    public OnTaxPayments(BigDecimal amountYouOwe, String reason) {
        this.amountYouOwe = amountYouOwe;
        this.reason = reason;
    }

    public BigDecimal getAmountYouOwe() {
        return amountYouOwe;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
