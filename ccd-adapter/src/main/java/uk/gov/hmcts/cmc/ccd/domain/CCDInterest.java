package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;

@Value
@Builder
public class CCDInterest {
    private CCDInterestType type;
    private CCDInterestBreakdown interestBreakdown;
    private BigDecimal rate;
    private String reason;
    private BigDecimal specificDailyAmount;
    private CCDInterestDate interestDate;
}
