package uk.gov.hmcts.cmc.domain.models.response;

import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class RepaymentPlan {

    private final BigDecimal instalmentAmount;
    private final LocalDate firstPaymentDate;
    private final PaymentSchedule paymentSchedule;

    public RepaymentPlan(BigDecimal instalmentAmount, LocalDate firstPaymentDate, PaymentSchedule paymentSchedule) {
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = paymentSchedule;
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RepaymentPlan that = (RepaymentPlan) other;
        return Objects.equals(instalmentAmount, that.instalmentAmount)
            && Objects.equals(firstPaymentDate, that.firstPaymentDate)
            && paymentSchedule == that.paymentSchedule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instalmentAmount, firstPaymentDate, paymentSchedule);
    }
}
