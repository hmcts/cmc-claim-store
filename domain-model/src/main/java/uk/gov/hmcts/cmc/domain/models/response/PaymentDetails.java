package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class PaymentDetails {
    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal paymentAmount;

    @DateNotInTheFuture
    private final LocalDate paymentDate;
    private final String paymentMethod;


    @JsonCreator
    public PaymentDetails(BigDecimal paymentAmount, LocalDate paymentDate, String paymentMethod) {
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public Optional<LocalDate> getPaymentDate() {
        return Optional.ofNullable(paymentDate);
    }

    public Optional<String> getPaymentMethod() {
        return Optional.ofNullable(paymentMethod);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PaymentDetails that = (PaymentDetails) other;
        return Objects.equals(paymentAmount, that.paymentAmount)
            && Objects.equals(paymentDate, that.paymentDate)
            && Objects.equals(paymentMethod, that.paymentMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentAmount, paymentDate, paymentMethod);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
