package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDExpense {
    private CCDExpenseType type;
    private String description;
    private CCDPaymentFrequency frequency;
    private BigDecimal amountPaid;
}
