package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpenseType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

@Component
public class ExpenseMapper {

    public CCDCollectionElement<CCDExpense> to(Expense expense) {
        if (expense == null) {
            return null;
        }
        return CCDCollectionElement.<CCDExpense>builder()
            .value(CCDExpense.builder()
                .type(CCDExpenseType.valueOf(expense.getType().name()))
                .amountPaid(expense.getAmount())
                .frequency(CCDPaymentFrequency.valueOf(expense.getFrequency().name()))
                .description(expense.getOtherName().orElse(null))
                .build())
            .id(expense.getId())
            .build();
    }

    public Expense from(CCDCollectionElement<CCDExpense> ccdExpense) {
        CCDExpense value = ccdExpense.getValue();
        if (value == null) {
            return null;
        }
        return Expense.builder()
            .id(ccdExpense.getId())
            .amount(value.getAmountPaid())
            .type(Expense.ExpenseType.valueOf(value.getType().name()))
            .frequency(PaymentFrequency.valueOf(value.getFrequency().name()))
            .otherName(value.getDescription())
            .build();
    }
}
