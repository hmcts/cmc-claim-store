package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class PaymentDeclaration {

    @NotNull
    @DateNotInTheFuture
    private final LocalDate paidDate;

    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal paidAmount;

    @NotBlank
    @Size(max = 99000)
    private final String explanation;

    public PaymentDeclaration(LocalDate paidDate, BigDecimal paidAmount, String explanation) {
        this.paidDate = paidDate;
        this.paidAmount = paidAmount;
        this.explanation = explanation;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public String getExplanation() {
        return explanation;
    }

    /**
     * The amount declared as having been paid. If this optional is empty, the amount can be assumed to be the full
     * claim amount.
     *
     * <pre>
     *     BigDecimal claimAmount = ...;
     *     BigDecimal paidAmount = paymentDeclaration.getPaidAmount().orElse(claimAmount);
     * </pre>
     *
     * @return the amount declared as having been paid, if available, else the empty optional implying the claim amount
     */
    public Optional<BigDecimal> getPaidAmount() {
        return Optional.ofNullable(paidAmount);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
