package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDDebt {
    private String description;
    private String totalOwed;
    private String monthlyPayments;
}
