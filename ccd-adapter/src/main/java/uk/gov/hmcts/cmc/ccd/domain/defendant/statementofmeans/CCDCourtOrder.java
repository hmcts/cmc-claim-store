package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDCourtOrder {
    private String claimNumber;
    private String amountOwed;
    private String monthlyInstalmentAmount;
}
