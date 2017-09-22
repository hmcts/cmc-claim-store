package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.ccj.RepaymentPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleRepaymentPlan {

    private BigDecimal remainingAmount = BigDecimal.valueOf(1000);
    private BigDecimal firstPayment = BigDecimal.valueOf(100);
    private BigDecimal instalmentAmount = BigDecimal.valueOf(100);
    private LocalDate firstPaymentDate = LocalDate.of(2100, 10, 10);
    private RepaymentPlan.PaymentSchedule paymentSchedule = RepaymentPlan.PaymentSchedule.EACH_WEEK;

    public static SampleRepaymentPlan builder() {
        return new SampleRepaymentPlan();
    }

    public SampleRepaymentPlan withRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
        return this;
    }

    public SampleRepaymentPlan withFirstPayment(BigDecimal firstPayment) {
        this.firstPayment = firstPayment;
        return this;
    }

    public SampleRepaymentPlan withInstalmentAmount(BigDecimal instalmentAmount) {
        this.instalmentAmount = instalmentAmount;
        return this;
    }

    public SampleRepaymentPlan withFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
        return this;
    }

    public SampleRepaymentPlan withPaymentSchedule(RepaymentPlan.PaymentSchedule paymentSchedule) {
        this.paymentSchedule = paymentSchedule;
        return this;
    }

    public RepaymentPlan build() {
        return new RepaymentPlan(remainingAmount, firstPayment, instalmentAmount, firstPaymentDate, paymentSchedule);
    }
}
