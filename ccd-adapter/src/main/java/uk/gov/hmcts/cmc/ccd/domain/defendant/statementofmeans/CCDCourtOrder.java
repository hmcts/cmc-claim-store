package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDCourtOrder {
    private String claimNumber;
    private BigDecimal amountOwed;
    private BigDecimal monthlyInstalmentAmount;
}
