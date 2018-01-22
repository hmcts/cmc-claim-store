package uk.gov.hmcts.cmc.ccd.domain.ccj;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDRepaymentPlan {

    private BigDecimal firstPayment;
    private BigDecimal instalmentAmount;
    private String firstPaymentDate;
    private CCDPaymentSchedule paymentSchedule;
}
