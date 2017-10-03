package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.claimstore.constraints.Money;
import uk.gov.hmcts.cmc.claimstore.constraints.ValidCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ValidCountyCourtJudgment
public class CountyCourtJudgment {

    @Valid
    @NotNull
    private final TheirDetails defendant;

    @Money
    @DecimalMin(value = "0.00")
    private final BigDecimal paidAmount;

    @NotNull
    private final PaymentOption paymentOption;

    @Valid
    private final RepaymentPlan repaymentPlan;

    @DateNotInThePast
    private final LocalDate payBySetDate;

    @Valid
    private final StatementOfTruth statementOfTruth;

    public CountyCourtJudgment(
        final TheirDetails defendant,
        final PaymentOption paymentOption,
        final BigDecimal paidAmount,
        final RepaymentPlan repaymentPlan,
        final LocalDate payBySetDate,
        final StatementOfTruth statementOfTruth
    ) {
        this.defendant = defendant;
        this.paymentOption = paymentOption;
        this.paidAmount = paidAmount;
        this.repaymentPlan = repaymentPlan;
        this.payBySetDate = payBySetDate;
        this.statementOfTruth = statementOfTruth;
    }

    public TheirDetails getDefendant() {
        return defendant;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public Optional<RepaymentPlan> getRepaymentPlan() {
        return Optional.ofNullable(repaymentPlan);
    }

    public Optional<LocalDate> getPayBySetDate() {
        return Optional.ofNullable(payBySetDate);
    }

    public Optional<StatementOfTruth> getStatementOfTruth() {
        return Optional.ofNullable(statementOfTruth);
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
        CountyCourtJudgment that = (CountyCourtJudgment) other;
        return Objects.equals(defendant, that.defendant)
            && Objects.equals(paymentOption, that.paymentOption)
            && Objects.equals(paidAmount, that.paidAmount)
            && Objects.equals(repaymentPlan, that.repaymentPlan)
            && Objects.equals(payBySetDate, that.payBySetDate)
            && Objects.equals(statementOfTruth, that.statementOfTruth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defendant, paymentOption, paidAmount, repaymentPlan, payBySetDate, statementOfTruth);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
