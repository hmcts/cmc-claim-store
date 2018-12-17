package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import java.math.BigDecimal;

@Builder
@Value
public class CCDExpense {
    private Expense.ExpenseType type;
    private String description;
    private PaymentFrequency frequency;
    private BigDecimal amountPaid;
}
