package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ExpenseAssert extends AbstractAssert<ExpenseAssert, Expense> {

    public ExpenseAssert(Expense actual) {
        super(actual, ExpenseAssert.class);
    }

    public ExpenseAssert isEqualTo(CCDExpense ccdExpense) {
        isNotNull();

        if (!Objects.equals(actual.getType().name(), ccdExpense.getType().name())) {
            failWithMessage("Expected Expense.type to be <%s> but was <%s>",
                ccdExpense.getType(), actual.getType());
        }

        if (!Objects.equals(actual.getFrequency().name(), ccdExpense.getFrequency().name())) {
            failWithMessage("Expected Expense.frequency to be <%s> but was <%s>",
                ccdExpense.getFrequency().name(), actual.getFrequency().name());
        }

        assertMoney(actual.getAmount())
            .isEqualTo(
                ccdExpense.getAmountPaid(),
                format("Expected Expense.amount to be <%s> but was <%s>",
                    ccdExpense.getAmountPaid(), actual.getAmount()
                )
            );

        if (!Objects.equals(actual.getOtherName().orElse(null), ccdExpense.getDescription())) {
            failWithMessage("Expected Expense.description to be <%s> but was <%s>",
                ccdExpense.getDescription(), actual.getOtherName());
        }
        return this;
    }
}
