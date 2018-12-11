package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDPayment {
    private String id;
    private BigDecimal amount;
    private String reference;
    private String dateCreated;
    private String status;
}
