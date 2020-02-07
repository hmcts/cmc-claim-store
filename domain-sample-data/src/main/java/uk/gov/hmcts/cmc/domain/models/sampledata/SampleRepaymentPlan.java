package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan.RepaymentPlanBuilder;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleRepaymentPlan {

    private SampleRepaymentPlan() {
        super();
    }

    public static RepaymentPlanBuilder builder() {
        return RepaymentPlan.builder()
            .instalmentAmount(new BigDecimal("100.99"))
            .paymentSchedule(PaymentSchedule.EACH_WEEK)
            .firstPaymentDate(LocalDate.of(2100, 10, 10))
            .completionDate(LocalDate.of(2101, 6, 10))
            .paymentLength("8 months")
            ;
    }
}
