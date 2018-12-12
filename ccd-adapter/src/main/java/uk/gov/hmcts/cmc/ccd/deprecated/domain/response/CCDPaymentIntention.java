package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepaymentPlan;

import java.time.LocalDate;

@Builder
@Value
public class CCDPaymentIntention {
    private CCDPaymentOption paymentOption;
    private LocalDate paymentDate;
    private CCDRepaymentPlan repaymentPlan;
}
