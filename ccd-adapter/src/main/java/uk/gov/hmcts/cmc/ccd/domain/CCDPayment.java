package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDPayment {
    private String id;
    private BigDecimal amount;
    private String reference;
    private String description;
    private String dateCreated;
    private CCDPaymentState paymentState;
    private String status;
}
