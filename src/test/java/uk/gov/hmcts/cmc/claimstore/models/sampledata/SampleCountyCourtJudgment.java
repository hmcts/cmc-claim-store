package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleCountyCourtJudgment {

    private TheirDetails defendant = SampleTheirDetails.builder().individualDetails();
    private BigDecimal paidAmount = BigDecimal.valueOf(0);
    private PaymentOption paymentOption = PaymentOption.IMMEDIATELY;
    private RepaymentPlan repaymentPlan;
    private LocalDate payBySetDate;
    private StatementOfTruth statementOfTruth;

    public static SampleCountyCourtJudgment builder() {
        return new SampleCountyCourtJudgment();
    }

    public SampleCountyCourtJudgment withDefendant(TheirDetails defendant) {
        this.defendant = defendant;
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
        this.paymentOption = PaymentOption.FULL_BY_SPECIFIED_DATE;
        this.payBySetDate = payBySetDate;
        this.repaymentPlan = null;
        return this;
    }

    public SampleCountyCourtJudgment withStatementOfThruth(StatementOfTruth statementOfThruth) {
        this.statementOfTruth = statementOfThruth;
        return this;
    }

    public CountyCourtJudgment build() {
        return new CountyCourtJudgment(
            defendant, paymentOption, paidAmount, repaymentPlan, payBySetDate, statementOfTruth
        );
    }
}
