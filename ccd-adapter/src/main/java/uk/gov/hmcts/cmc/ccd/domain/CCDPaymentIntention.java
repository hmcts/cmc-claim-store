package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

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
