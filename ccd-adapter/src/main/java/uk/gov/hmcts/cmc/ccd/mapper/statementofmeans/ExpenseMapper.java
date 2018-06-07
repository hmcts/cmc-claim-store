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
            .type(expense.getType())
            .amountPaid(expense.getAmountPaid())
            .frequency(CCDPaymentFrequency.valueOf(expense.getFrequency().name()))
            .build();
    }

    @Override
    public Expense from(CCDExpense ccdExpense) {
        return new Expense(
            ccdExpense.getType(),
            PaymentFrequency.valueOf(ccdExpense.getFrequency().name()),
            ccdExpense.getAmountPaid()
        );
    }
}
