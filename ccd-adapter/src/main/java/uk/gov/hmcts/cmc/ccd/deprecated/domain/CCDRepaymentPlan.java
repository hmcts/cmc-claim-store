package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CCDRepaymentPlan {

    private BigDecimal instalmentAmount;
    private LocalDate firstPaymentDate;
    private CCDPaymentSchedule paymentSchedule;
    private LocalDate completionDate;
    private String paymentLength;
}
