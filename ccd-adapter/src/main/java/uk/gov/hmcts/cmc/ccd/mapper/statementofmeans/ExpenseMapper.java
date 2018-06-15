package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

@Component
public class ExpenseMapper implements Mapper<CCDExpense, Expense> {

    @Override
    public CCDExpense to(Expense expense) {
        return CCDExpense.builder()
            .type(expense.getType())
            .amountPaid(expense.getAmountPaid())
            .frequency(expense.getFrequency())
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
            .type(ccdExpense.getType())
            .frequency(ccdExpense.getFrequency())
            .otherExpense(ccdExpense.getOtherExpense())
            .build();
    }
}
