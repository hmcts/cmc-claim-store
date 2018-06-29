package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class PaymentDetails {
    private final BigDecimal paidAmount;
    private final LocalDate paymentDate;
    private final String paymentMethod;


    @JsonCreator
    public PaymentDetails(BigDecimal paidAmount, LocalDate paymentDate, String paymentMethod) {
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
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
        return Objects.equals(paidAmount, that.paidAmount) &&
            Objects.equals(paymentDate, that.paymentDate) &&
            Objects.equals(paymentMethod, that.paymentMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paidAmount, paymentDate, paymentMethod);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
