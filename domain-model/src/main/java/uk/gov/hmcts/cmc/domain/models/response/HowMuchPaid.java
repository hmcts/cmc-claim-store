package uk.gov.hmcts.cmc.domain.models.response;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HowMuchPaid {

    @Money
    @NotNull
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    @NotNull
    @DateNotInTheFuture
    private final LocalDate pastDate;

    @NotBlank
    @Size(max = 99000)
    private final String explanation;

    public HowMuchPaid(BigDecimal amount, LocalDate pastDate, String explanation) {
        this.amount = amount;
        this.pastDate = pastDate;
        this.explanation = explanation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getExplanation() {
        return explanation;
    }

    public LocalDate getPastDate() {
        return pastDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HowMuchPaid that = (HowMuchPaid) obj;

        return Objects.equals(this.amount, that.amount)
            && Objects.equals(this.pastDate, that.pastDate)
            && Objects.equals(this.explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, pastDate, explanation);
    }

}
