package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import java.time.LocalDate;

public class SampleCountyCourtJudgment {

    private TheirDetails defendant = SampleTheirDetails.builder().individualDetails();
    private double paidAmount = 0.0;
    private PaymentOption paymentOption = PaymentOption.IMMEDIATELY;
    private RepaymentPlan repaymentPlan = null;
    private LocalDate payBySetDate = null;

    public static SampleCountyCourtJudgment builder() {
        return new SampleCountyCourtJudgment();
    }

    public SampleCountyCourtJudgment withDefendant(TheirDetails defendant) {
        this.defendant = defendant;
        return this;
    }

    public SampleCountyCourtJudgment withPaidAmount(double paidAmount) {
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
        this.paymentOption = PaymentOption.FULL;
        this.payBySetDate = payBySetDate;
        this.repaymentPlan = null;
        return this;
    }

    public CountyCourtJudgment build() {
        return new CountyCourtJudgment(defendant, paymentOption, paidAmount, repaymentPlan, payBySetDate);
    }
}
