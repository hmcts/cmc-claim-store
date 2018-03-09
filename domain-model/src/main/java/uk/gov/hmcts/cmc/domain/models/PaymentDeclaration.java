package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class PaymentDeclaration {

    @NotNull
    @DateNotInTheFuture
    private final LocalDate paidDate;

    @NotBlank
    @Size(max = 99000)
    private final String explanation;

    public PaymentDeclaration(LocalDate paidDate, String explanation) {
        this.paidDate = paidDate;
        this.explanation = explanation;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final PaymentDeclaration that = (PaymentDeclaration) other;
        return Objects.equals(paidDate, that.paidDate)
            && Objects.equals(explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paidDate, explanation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
