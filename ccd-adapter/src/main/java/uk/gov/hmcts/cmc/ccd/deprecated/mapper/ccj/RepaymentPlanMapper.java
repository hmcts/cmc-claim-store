package uk.gov.hmcts.cmc.ccd.deprecated.mapper.ccj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentSchedule.valueOf;

@Component
public class RepaymentPlanMapper implements Mapper<CCDRepaymentPlan, RepaymentPlan> {

    @Override
    public CCDRepaymentPlan to(RepaymentPlan repaymentPlan) {
        CCDRepaymentPlan.CCDRepaymentPlanBuilder builder = CCDRepaymentPlan.builder();
        repaymentPlan.getCompletionDate().ifPresent(builder::completionDate);
        repaymentPlan.getPaymentLength().ifPresent(builder::paymentLength);

        return builder
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

        RepaymentPlan.RepaymentPlanBuilder builder = RepaymentPlan.builder();
        Optional.ofNullable(ccdRepaymentPlan.getCompletionDate()).ifPresent(builder::completionDate);
        Optional.ofNullable(ccdRepaymentPlan.getPaymentLength()).ifPresent(builder::paymentLength);

        return builder
            .instalmentAmount(ccdRepaymentPlan.getInstalmentAmount())
            .firstPaymentDate(ccdRepaymentPlan.getFirstPaymentDate())
            .paymentSchedule(PaymentSchedule.valueOf(ccdRepaymentPlan.getPaymentSchedule().name()))
            .build();
    }
}
