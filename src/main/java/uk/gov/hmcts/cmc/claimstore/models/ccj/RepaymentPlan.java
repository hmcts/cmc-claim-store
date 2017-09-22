package uk.gov.hmcts.cmc.claimstore.models.ccj;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.claimstore.constraints.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RepaymentPlan {

    public enum PaymentSchedule {
        EACH_WEEK,
        EVERY_TWO_WEEKS,
        EVERY_MONTH
    }

    @NotNull
    @Money
    private final BigDecimal remainingAmount;

    @NotNull
    @Money
    private final BigDecimal firstPayment;

    @NotNull
    @Money
    private final BigDecimal instalmentAmount;

    @NotNull
    @DateNotInThePast
    private final LocalDate firstPaymentDate;

    @NotNull
    private final PaymentSchedule paymentSchedule;

    public RepaymentPlan(final BigDecimal remainingAmount,
                         final BigDecimal firstPayment,
                         final BigDecimal instalmentAmount,
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
