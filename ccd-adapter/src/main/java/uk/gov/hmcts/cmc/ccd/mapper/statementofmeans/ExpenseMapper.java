package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

@Component
public class ExpenseMapper implements Mapper<CCDExpense, Expense> {

    @Override
    public CCDExpense to(Expense expense) {
        return CCDExpense.builder()
            .type(CCDExpense.ExpenseType.valueOf(expense.getType().name()))
            .amountPaid(expense.getAmountPaid())
            .frequency(CCDPaymentFrequency.valueOf(expense.getFrequency().name()))
            .otherExpense(expense.getOtherExpense().orElse(null))
            .build();
    }

    @Override
    public Expense from(CCDExpense ccdExpense) {
        if (ccdExpense == null) {
            return null;
        }
        return Expense.builder()
            .amountPaid(ccdExpense.getAmountPaid())
            .type(Expense.ExpenseType.valueOf(ccdExpense.getType().name()))
            .frequency(PaymentFrequency.valueOf(ccdExpense.getFrequency().name()))
            .otherExpense(ccdExpense.getOtherExpense())
            .build();
    }
}
