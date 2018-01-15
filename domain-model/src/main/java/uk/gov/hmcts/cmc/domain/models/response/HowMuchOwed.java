package uk.gov.hmcts.cmc.domain.models.response;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class HowMuchOwed {

    @Money
    @NotNull
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    @NotBlank
    @Size(max = 99000)
    private final String explanation;

    public HowMuchOwed(BigDecimal amount, String explanation) {
        this.amount = amount;
        this.explanation = explanation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HowMuchOwed that = (HowMuchOwed) obj;

        return Objects.equals(this.amount, that.amount)
            && Objects.equals(this.explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, explanation);
    }

}
