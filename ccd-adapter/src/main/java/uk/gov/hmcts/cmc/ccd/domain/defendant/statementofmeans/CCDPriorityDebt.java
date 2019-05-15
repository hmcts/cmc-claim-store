package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPriorityDebt {
    private CCDPriorityDebtType type;
    private CCDPaymentFrequency frequency;
    private String amount;
}
