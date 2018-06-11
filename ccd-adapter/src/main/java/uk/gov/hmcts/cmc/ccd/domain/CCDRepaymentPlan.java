package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CCDRepaymentPlan {

    private BigDecimal instalmentAmount;
    private LocalDate firstPaymentDate;
    private CCDPaymentSchedule paymentSchedule;
}
