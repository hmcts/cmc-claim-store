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
    private final BigDecimal amount;

    @DateNotInTheFuture
    private final LocalDate date;
    private final String paymentMethod;


    @JsonCreator
    public PaymentDetails(BigDecimal amount, LocalDate date, String paymentMethod) {
        this.amount = amount;
        this.date = date;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
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
        return Objects.equals(amount, that.amount)
            && Objects.equals(date, that.date)
            && Objects.equals(paymentMethod, that.paymentMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, date, paymentMethod);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
