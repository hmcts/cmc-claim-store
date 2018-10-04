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
            .instalmentAmount(BigDecimal.valueOf(100))
            .paymentSchedule(PaymentSchedule.EACH_WEEK)
            .firstPaymentDate(LocalDate.of(2100, 10, 10));
    }
}
