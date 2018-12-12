package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.ccj.RepaymentPlanMapper;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

@Component
public class PaymentIntentionMapper implements Mapper<CCDPaymentIntention, PaymentIntention> {

    private final RepaymentPlanMapper repaymentPlanMapper;

    @Autowired
    public PaymentIntentionMapper(RepaymentPlanMapper repaymentPlanMapper) {
        this.repaymentPlanMapper = repaymentPlanMapper;
    }


    @Override
    public CCDPaymentIntention to(PaymentIntention paymentIntention) {
        return CCDPaymentIntention.builder()
            .paymentOption(CCDPaymentOption.valueOf(paymentIntention.getPaymentOption().name()))
            .paymentDate(paymentIntention.getPaymentDate().orElse(null))
            .repaymentPlan(paymentIntention.getRepaymentPlan().map(repaymentPlanMapper::to).orElse(null))
            .build();
    }

    @Override
    public PaymentIntention from(CCDPaymentIntention ccdFullAdmissionResponse) {
        return PaymentIntention.builder()
            .paymentOption(PaymentOption.valueOf(ccdFullAdmissionResponse.getPaymentOption().name()))
            .paymentDate(ccdFullAdmissionResponse.getPaymentDate())
            .repaymentPlan(repaymentPlanMapper.from(ccdFullAdmissionResponse.getRepaymentPlan()))
            .build();
    }
}
