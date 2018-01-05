package uk.gov.hmcts.cmc.domain.models.response;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PayBySetDate {

    @NotNull
    @DateNotInThePast
    private final LocalDate paymentDate;

    @NotBlank()
    @Size(max = 99000)
    private final String explanation;

    public PayBySetDate(LocalDate paymentDate, String explanation) {
        this.paymentDate = paymentDate;
        this.explanation = explanation;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
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

        PayBySetDate that = (PayBySetDate) obj;

        return Objects.equals(this.paymentDate, that.paymentDate)
            && Objects.equals(this.explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentDate, explanation);
    }

}
