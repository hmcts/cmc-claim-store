package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDExpense {
    private CCDExpenseType type;
    private String description;
    private CCDPaymentFrequency frequency;
    private String amountPaid;
}
