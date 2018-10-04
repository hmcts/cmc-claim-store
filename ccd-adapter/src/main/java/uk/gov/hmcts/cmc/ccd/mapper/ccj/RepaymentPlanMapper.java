package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import static uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule.valueOf;

@Component
public class RepaymentPlanMapper implements Mapper<CCDRepaymentPlan, RepaymentPlan> {

    @Override
    public CCDRepaymentPlan to(RepaymentPlan repaymentPlan) {
        return CCDRepaymentPlan
            .builder()
            .instalmentAmount(repaymentPlan.getInstalmentAmount())
            .firstPaymentDate(repaymentPlan.getFirstPaymentDate())
            .paymentSchedule(valueOf(repaymentPlan.getPaymentSchedule().name()))
            .build();
    }

    @Override
    public RepaymentPlan from(CCDRepaymentPlan ccdRepaymentPlan) {
        if (ccdRepaymentPlan == null) {
            return null;
        }

        return RepaymentPlan.builder()
            .instalmentAmount(ccdRepaymentPlan.getInstalmentAmount())
            .firstPaymentDate(ccdRepaymentPlan.getFirstPaymentDate())
            .paymentSchedule(PaymentSchedule.valueOf(ccdRepaymentPlan.getPaymentSchedule().name()))
            .completionDate(ccdRepaymentPlan.getCompletionDate())
            .build();
    }
}
