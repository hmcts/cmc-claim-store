package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDCourtOrder {

    private String details;
    private BigDecimal amountOwed;
    private BigDecimal monthlyInstalmentAmount;
}
