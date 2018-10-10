package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.constraints.ValidCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
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

    @Builder
    public CountyCourtJudgment(
        LocalDate defendantDateOfBirth,
        PaymentOption paymentOption,
        BigDecimal paidAmount,
        RepaymentPlan repaymentPlan,
        LocalDate payBySetDate,
        StatementOfTruth statementOfTruth
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
