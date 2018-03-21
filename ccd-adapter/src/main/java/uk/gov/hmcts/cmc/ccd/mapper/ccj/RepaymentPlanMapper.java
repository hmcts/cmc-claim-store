package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import static uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentSchedule.valueOf;

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

        return new RepaymentPlan(
            ccdRepaymentPlan.getInstalmentAmount(),
            ccdRepaymentPlan.getFirstPaymentDate(),
            PaymentSchedule.valueOf(ccdRepaymentPlan.getPaymentSchedule().name())
        );
    }
}
