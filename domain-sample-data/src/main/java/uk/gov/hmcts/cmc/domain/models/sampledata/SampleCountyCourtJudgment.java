package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleCountyCourtJudgment {

    private LocalDate defendantDateOfBirth;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private PaymentOption paymentOption = PaymentOption.IMMEDIATELY;
    private RepaymentPlan repaymentPlan;
    private LocalDate payBySetDate;
    private StatementOfTruth statementOfTruth;

    public static SampleCountyCourtJudgment builder() {
        return new SampleCountyCourtJudgment();
    }

    public SampleCountyCourtJudgment withDefendantDateOfBirth(LocalDate defendantDateOfBirth) {
        this.defendantDateOfBirth = defendantDateOfBirth;
        return this;
    }

    public SampleCountyCourtJudgment withPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
        return this;
    }

    public SampleCountyCourtJudgment withPaymentOptionImmediately() {
        this.paymentOption = PaymentOption.IMMEDIATELY;
        this.repaymentPlan = null;
        this.payBySetDate = null;
        return this;
    }

    public SampleCountyCourtJudgment withRepaymentPlan(RepaymentPlan repaymentPlan) {
        this.paymentOption = PaymentOption.INSTALMENTS;
        this.repaymentPlan = repaymentPlan;
        this.payBySetDate = null;
        return this;
    }

    public SampleCountyCourtJudgment withPayBySetDate(LocalDate payBySetDate) {
        this.paymentOption = PaymentOption.BY_SPECIFIED_DATE;
        this.payBySetDate = payBySetDate;
        this.repaymentPlan = null;
        return this;
    }

    public SampleCountyCourtJudgment withPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
        return this;
    }

    public SampleCountyCourtJudgment withStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.statementOfTruth = statementOfTruth;
        return this;
    }

    public CountyCourtJudgment build() {
        return new CountyCourtJudgment(
            defendantDateOfBirth, paymentOption, paidAmount, repaymentPlan, payBySetDate, statementOfTruth
        );
    }
}
