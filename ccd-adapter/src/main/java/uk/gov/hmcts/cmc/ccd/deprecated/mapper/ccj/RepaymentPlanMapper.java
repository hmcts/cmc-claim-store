package uk.gov.hmcts.cmc.ccd.deprecated.mapper.ccj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentSchedule.valueOf;

@Component
public class RepaymentPlanMapper implements Mapper<CCDRepaymentPlan, RepaymentPlan> {

    @Override
    public CCDRepaymentPlan to(RepaymentPlan repaymentPlan) {
        return CCDRepaymentPlan
            .builder()
            .instalmentAmount(repaymentPlan.getInstalmentAmount())
            .firstPaymentDate(repaymentPlan.getFirstPaymentDate())
            .paymentSchedule(valueOf(repaymentPlan.getPaymentSchedule().name()))
            .completionDate(repaymentPlan.getCompletionDate())
            .paymentLength(repaymentPlan.getPaymentLength())
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
            .paymentLength(ccdRepaymentPlan.getPaymentLength())
            .build();
    }
}
