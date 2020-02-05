package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.ValidPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.time.LocalDate;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@ValidPaymentIntention
public class PaymentIntention {

    @NotNull
    private final PaymentOption paymentOption;

    @DateNotInThePast(groups = PaymentIntention.Proposing.class)
    private final LocalDate paymentDate;

    @Valid
    private final RepaymentPlan repaymentPlan;

    @JsonCreator
    public PaymentIntention(PaymentOption paymentOption, LocalDate paymentDate, RepaymentPlan repaymentPlan) {
        this.paymentOption = paymentOption;
        this.paymentDate = paymentDate;
        this.repaymentPlan = repaymentPlan;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public Optional<LocalDate> getPaymentDate() {
        return Optional.ofNullable(paymentDate);
    }

    public Optional<RepaymentPlan> getRepaymentPlan() {
        return Optional.ofNullable(repaymentPlan);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

    public interface Proposing {
    }

    public interface Responding {
    }
}
