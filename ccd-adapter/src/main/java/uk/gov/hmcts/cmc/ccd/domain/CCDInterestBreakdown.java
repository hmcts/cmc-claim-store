package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDInterestBreakdown {
    private BigDecimal totalAmount;
    private String explanation;
}
