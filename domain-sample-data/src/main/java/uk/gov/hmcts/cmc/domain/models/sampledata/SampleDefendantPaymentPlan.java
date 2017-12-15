package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.response.DefendantPaymentPlan;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleDefendantPaymentPlan {

    private BigDecimal firstPayment = BigDecimal.valueOf(100);
    private BigDecimal instalmentAmount = BigDecimal.valueOf(100);
    private LocalDate firstPaymentDate = LocalDate.of(2100, 10, 10);
    private PaymentSchedule paymentSchedule = PaymentSchedule.EACH_WEEK;
    private String explanation = "My explanation";

    public static SampleDefendantPaymentPlan builder() {
        return new SampleDefendantPaymentPlan();
    }

    public SampleDefendantPaymentPlan withFirstPayment(BigDecimal firstPayment) {
        this.firstPayment = firstPayment;
        return this;
    }

    public SampleDefendantPaymentPlan withInstalmentAmount(BigDecimal instalmentAmount) {
        this.instalmentAmount = instalmentAmount;
        return this;
    }

    public SampleDefendantPaymentPlan withFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
        return this;
    }

    public SampleDefendantPaymentPlan withPaymentSchedule(PaymentSchedule paymentSchedule) {
        this.paymentSchedule = paymentSchedule;
        return this;
    }

    public SampleDefendantPaymentPlan withExplantion(String explanation) {
        this.explanation = explanation;
        return this;
    }

    public DefendantPaymentPlan build() {
        return new DefendantPaymentPlan(firstPayment, instalmentAmount, firstPaymentDate, paymentSchedule, explanation);
    }


}
