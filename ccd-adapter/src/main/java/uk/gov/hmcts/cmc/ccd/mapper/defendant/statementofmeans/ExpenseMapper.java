package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

@Component
public class ExpenseMapper {

    public CCDExpense to(Expense expense) {
        return CCDExpense.builder()
            .type(expense.getType())
            .amountPaid(expense.getAmount())
            .frequency(expense.getFrequency())
            .description(expense.getOtherName().orElse(null))
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
            .type(value.getType())
            .frequency(value.getFrequency())
            .otherName(value.getDescription())
            .build();
    }
}
