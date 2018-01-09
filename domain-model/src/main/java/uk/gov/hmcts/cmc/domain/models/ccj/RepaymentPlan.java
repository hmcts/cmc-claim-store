package uk.gov.hmcts.cmc.domain.models.ccj;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class RepaymentPlan {

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal firstPayment;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal instalmentAmount;

    @NotNull
    @DateNotInThePast
    private final LocalDate firstPaymentDate;

    @NotNull
    private final PaymentSchedule paymentSchedule;

    public RepaymentPlan(
        BigDecimal firstPayment,
        BigDecimal instalmentAmount,
        LocalDate firstPaymentDate,
        PaymentSchedule paymentSchedule
    ) {

        this.firstPayment = firstPayment;
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = paymentSchedule;
    }

    public BigDecimal getFirstPayment() {
        return firstPayment;
    }

    public BigDecimal getInstalmentAmount() {
        return instalmentAmount;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public PaymentSchedule getPaymentSchedule() {
        return paymentSchedule;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        RepaymentPlan that = (RepaymentPlan) other;
        return Objects.equals(firstPayment, that.firstPayment)
            && Objects.equals(instalmentAmount, that.instalmentAmount)
            && Objects.equals(firstPaymentDate, that.firstPaymentDate)
            && Objects.equals(paymentSchedule, that.paymentSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstPayment, instalmentAmount, firstPaymentDate, paymentSchedule);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
