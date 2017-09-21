package uk.gov.hmcts.cmc.claimstore.models.ccj;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RepaymentPlan {

    public enum PaymentSchedule {
        EACH_WEEK,
        EVERY_TWO_WEEKS,
        EVERY_MONTH
    }

    private final double remainingAmount;
    private final double firstPayment;
    private final double instalmentAmount;
    private final LocalDate firstPaymentDate;
    private final PaymentSchedule paymentSchedule;

    public RepaymentPlan(final double remainingAmount,
                         final double firstPayment,
                         final double instalmentAmount,
                         final LocalDate firstPaymentDate,
                         final PaymentSchedule paymentSchedule) {

        this.remainingAmount = remainingAmount;
        this.firstPayment = firstPayment;
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = paymentSchedule;
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
        return Objects.equals(remainingAmount, that.remainingAmount)
            && Objects.equals(firstPayment, that.firstPayment)
            && Objects.equals(instalmentAmount, that.instalmentAmount)
            && Objects.equals(firstPaymentDate, that.firstPaymentDate)
            && Objects.equals(paymentSchedule, that.paymentSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingAmount, firstPayment, instalmentAmount, firstPaymentDate, paymentSchedule);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
