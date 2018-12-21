package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Optional;

@Component
public class PaymentIntentionMapper implements Mapper<CCDPaymentIntention, PaymentIntention> {

    @Override
    public CCDPaymentIntention to(PaymentIntention paymentIntention) {
        CCDPaymentIntention.CCDPaymentIntentionBuilder builder = CCDPaymentIntention.builder();

        builder.paymentOption(paymentIntention.getPaymentOption());

        paymentIntention.getPaymentDate().ifPresent(builder::paymentDate);

        paymentIntention.getRepaymentPlan().ifPresent(repaymentPlan -> {
            builder
                .instalmentAmount(repaymentPlan.getInstalmentAmount())
                .firstPaymentDate(repaymentPlan.getFirstPaymentDate())
                .paymentSchedule(repaymentPlan.getPaymentSchedule());

            repaymentPlan.getCompletionDate().ifPresent(builder::completionDate);
            repaymentPlan.getPaymentLength().ifPresent(builder::paymentLength);
        });
        return builder.build();
    }

    @Override
    public PaymentIntention from(CCDPaymentIntention ccdPaymentIntention) {
        PaymentIntention.PaymentIntentionBuilder builder = PaymentIntention.builder();

        if (ccdPaymentIntention.hasRepaymentPlanInfo()) {
            RepaymentPlan.RepaymentPlanBuilder planBuilder = RepaymentPlan.builder()
                .paymentSchedule(ccdPaymentIntention.getPaymentSchedule())
                .firstPaymentDate(ccdPaymentIntention.getFirstPaymentDate())
                .instalmentAmount(ccdPaymentIntention.getInstalmentAmount());

            Optional.ofNullable(ccdPaymentIntention.getCompletionDate()).ifPresent(planBuilder::completionDate);
            Optional.ofNullable(ccdPaymentIntention.getPaymentLength()).ifPresent(planBuilder::paymentLength);

            builder.repaymentPlan(planBuilder.build());
        }

        builder.paymentOption(ccdPaymentIntention.getPaymentOption());
        Optional.ofNullable(ccdPaymentIntention.getPaymentDate()).ifPresent(builder::paymentDate);
        return builder.build();
    }
}
