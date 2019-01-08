package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;


@Component
public class PaymentIntentionMapper implements Mapper<CCDPaymentIntention, PaymentIntention> {

    @Override
    public CCDPaymentIntention to(PaymentIntention paymentIntention) {
        if (null == paymentIntention) {
            return null;
        }
        CCDPaymentIntention.CCDPaymentIntentionBuilder builder = CCDPaymentIntention.builder();
        builder.paymentOption(CCDPaymentOption.valueOf(paymentIntention.getPaymentOption().name()));
        builder.paymentDate(paymentIntention.getPaymentDate().orElse(null));
        paymentIntention.getRepaymentPlan().ifPresent(repaymentPlan ->
            builder.instalmentAmount(repaymentPlan.getInstalmentAmount())
                .firstPaymentDate(repaymentPlan.getFirstPaymentDate())
                .paymentSchedule(CCDPaymentSchedule.valueOf(repaymentPlan.getPaymentSchedule().name()))
                .completionDate(repaymentPlan.getCompletionDate())
                .paymentLength(repaymentPlan.getPaymentLength())
        );
        return builder.build();
    }

    @Override
    public PaymentIntention from(CCDPaymentIntention ccdPaymentIntention) {
        if (null == ccdPaymentIntention) {
            return null;
        }
        PaymentIntention.PaymentIntentionBuilder builder = PaymentIntention.builder();
        if (ccdPaymentIntention.hasRepaymentPlanInfo()) {
            builder.repaymentPlan(RepaymentPlan.builder()
                .paymentSchedule(PaymentSchedule.valueOf(ccdPaymentIntention.getPaymentSchedule().name()))
                .firstPaymentDate(ccdPaymentIntention.getFirstPaymentDate())
                .instalmentAmount(ccdPaymentIntention.getInstalmentAmount())
                .completionDate(ccdPaymentIntention.getCompletionDate())
                .paymentLength(ccdPaymentIntention.getPaymentLength())
                .build());
        }
        builder.paymentOption(PaymentOption.valueOf(ccdPaymentIntention.getPaymentOption().name()));
        builder.paymentDate(ccdPaymentIntention.getPaymentDate());
        return builder.build();
    }
}
