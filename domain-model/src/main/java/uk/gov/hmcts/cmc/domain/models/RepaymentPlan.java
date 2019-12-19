package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class RepaymentPlan {

    @NotNull
    @Money
    @DecimalMin(value = "1.00")
    private final BigDecimal instalmentAmount;

    @NotNull
    @DateNotInThePast(groups = PaymentIntention.Proposing.class)
    private final LocalDate firstPaymentDate;

    @NotNull
    private final PaymentSchedule paymentSchedule;

    private final LocalDate completionDate;

    private final String paymentLength;

    @Builder
    public RepaymentPlan(
        BigDecimal instalmentAmount,
        LocalDate firstPaymentDate,
        PaymentSchedule paymentSchedule,
        LocalDate completionDate,
        String paymentLength
    ) {
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = paymentSchedule;
        this.completionDate = completionDate;
        this.paymentLength = paymentLength;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
