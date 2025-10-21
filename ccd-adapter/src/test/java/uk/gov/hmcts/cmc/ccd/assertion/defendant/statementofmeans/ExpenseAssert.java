package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ExpenseAssert extends CustomAssert<ExpenseAssert, Expense> {

    public ExpenseAssert(Expense actual) {
        super("Expense", actual, ExpenseAssert.class);
    }

    public ExpenseAssert isEqualTo(CCDExpense expected) {
        isNotNull();

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        compare("frequency",
            expected.getFrequency(), Enum::name,
            Optional.ofNullable(actual.getFrequency()).map(Enum::name));

        compare("amount",
            expected.getAmountPaid(),
            Optional.ofNullable(actual.getAmount()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("description",
            expected.getDescription(),
            actual.getOtherName());

        return this;
    }
}
