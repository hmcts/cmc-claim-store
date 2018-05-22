package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

public class ExpenseMapper implements Mapper<CCDExpense, Expense> {
    @Override
    public CCDExpense to(Expense expense) {
        return null;
    }

    @Override
    public Expense from(CCDExpense ccdExpense) {
        return null;
    }
}
