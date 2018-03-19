package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;

@Value
@Builder
public class CCDInterest {
    private CCDInterestType type;
    private BigDecimal rate;
    private String reason;
}
