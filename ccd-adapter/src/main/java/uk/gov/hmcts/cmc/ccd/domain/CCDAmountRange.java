package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDAmountRange {
    private BigDecimal lowerValue;
    private BigDecimal higherValue;
}
