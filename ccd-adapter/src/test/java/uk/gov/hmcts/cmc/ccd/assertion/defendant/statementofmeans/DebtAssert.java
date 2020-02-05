package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class DebtAssert extends CustomAssert<DebtAssert, Debt> {

    public DebtAssert(Debt actual) {
        super("Debt", actual, DebtAssert.class);
    }

    public DebtAssert isEqualTo(CCDDebt expected) {
        isNotNull();

        compare("description",
            expected.getDescription(),
            Optional.ofNullable(actual.getDescription()));

        compare("totalOwed",
            expected.getTotalOwed(),
            Optional.ofNullable(actual.getTotalOwed()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("monthlyPayments",
            expected.getMonthlyPayments(),
            Optional.ofNullable(actual.getMonthlyPayments()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        return this;
    }
}
