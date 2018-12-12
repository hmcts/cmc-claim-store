package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Value
public class CCDPaymentIntention {
    private CCDPaymentOption paymentOption;
    private LocalDate paymentDate;
    private BigDecimal instalmentAmount;
    private LocalDate firstPaymentDate;
    private CCDPaymentSchedule paymentSchedule;
    private LocalDate completionDate;
    private String paymentLength;
}
