package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.models.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.constraints.ValidCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ValidCountyCourtJudgment
public class CountyCourtJudgment {

    @AgeRangeValidator
    private final LocalDate defendantDateOfBirth;

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
        final LocalDate defendantDateOfBirth,
        final PaymentOption paymentOption,
        final BigDecimal paidAmount,
        final RepaymentPlan repaymentPlan,
        final LocalDate payBySetDate,
        final StatementOfTruth statementOfTruth
    ) {
        this.defendantDateOfBirth = defendantDateOfBirth;
        this.paymentOption = paymentOption;
        this.paidAmount = paidAmount;
        this.repaymentPlan = repaymentPlan;
        this.payBySetDate = payBySetDate;
        this.statementOfTruth = statementOfTruth;
    }

    public Optional<LocalDate> getDefendantDateOfBirth() {
        return Optional.ofNullable(defendantDateOfBirth);
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public Optional<BigDecimal> getPaidAmount() {
        return Optional.ofNullable(paidAmount);
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
        return Objects.equals(defendantDateOfBirth, that.defendantDateOfBirth)
            && Objects.equals(paymentOption, that.paymentOption)
            && Objects.equals(paidAmount, that.paidAmount)
            && Objects.equals(repaymentPlan, that.repaymentPlan)
            && Objects.equals(payBySetDate, that.payBySetDate)
            && Objects.equals(statementOfTruth, that.statementOfTruth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defendantDateOfBirth, paymentOption, paidAmount, repaymentPlan, payBySetDate,
            statementOfTruth);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
