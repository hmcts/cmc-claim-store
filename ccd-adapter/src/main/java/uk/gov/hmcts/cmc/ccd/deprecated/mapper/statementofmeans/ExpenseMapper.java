package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

@Component
public class ExpenseMapper implements Mapper<CCDExpense, Expense> {

    @Override
    public CCDExpense to(Expense expense) {
        return CCDExpense.builder()
            .type(expense.getType())
            .amountPaid(expense.getAmount())
            .frequency(expense.getFrequency())
            .description(expense.getOtherName().orElse(null))
            .build();
    }

    @Override
    public Expense from(CCDExpense ccdExpense) {
        if (ccdExpense == null) {
            return null;
        }
        return Expense.builder()
            .amount(ccdExpense.getAmountPaid())
            .type(ccdExpense.getType())
            .frequency(ccdExpense.getFrequency())
            .otherName(ccdExpense.getDescription())
            .build();
    }
}
