package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@JsonIgnoreProperties(value = {"firstPayment"})
public class RepaymentPlan {

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal instalmentAmount;

    @NotNull
    @DateNotInThePast
    private final LocalDate firstPaymentDate;

    @NotNull
    private final PaymentSchedule paymentSchedule;

    @Builder
    public RepaymentPlan(
        BigDecimal instalmentAmount,
        LocalDate firstPaymentDate,
        PaymentSchedule paymentSchedule
    ) {
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
