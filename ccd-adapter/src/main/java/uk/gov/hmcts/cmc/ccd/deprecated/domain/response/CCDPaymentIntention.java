package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;

import java.time.LocalDate;

@Builder
@Value
public class CCDPaymentIntention {
    private CCDPaymentOption paymentOption;
    private LocalDate paymentDate;
    private CCDRepaymentPlan repaymentPlan;
}

