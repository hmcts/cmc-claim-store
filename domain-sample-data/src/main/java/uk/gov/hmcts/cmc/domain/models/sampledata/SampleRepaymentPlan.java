package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleRepaymentPlan {

    private BigDecimal instalmentAmount = BigDecimal.valueOf(100);
    private LocalDate firstPaymentDate = LocalDate.of(2100, 10, 10);
    private PaymentSchedule paymentSchedule = PaymentSchedule.EACH_WEEK;

    public static SampleRepaymentPlan builder() {
        return new SampleRepaymentPlan();
    }

    public SampleRepaymentPlan withInstalmentAmount(BigDecimal instalmentAmount) {
        this.instalmentAmount = instalmentAmount;
        return this;
    }

    public SampleRepaymentPlan withFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
        return this;
    }

    public SampleRepaymentPlan withPaymentSchedule(PaymentSchedule paymentSchedule) {
        this.paymentSchedule = paymentSchedule;
        return this;
    }

    public RepaymentPlan build() {
        return new RepaymentPlan(instalmentAmount, firstPaymentDate, paymentSchedule);
    }
}
