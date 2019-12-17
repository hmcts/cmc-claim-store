package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class IncomeAssert extends CustomAssert<IncomeAssert, Income> {

    public IncomeAssert(Income actual) {
        super("Income", actual, IncomeAssert.class);
    }

    public IncomeAssert isEqualTo(CCDIncome expected) {
        isNotNull();

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        compare("frequency",
            expected.getFrequency(), Enum::name,
            Optional.ofNullable(actual.getFrequency()).map(Enum::name));

        compare("amount",
            expected.getAmountReceived(),
            Optional.ofNullable(actual.getAmount()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("otherSource",
            expected.getOtherSource(),
            actual.getOtherSource());

        return this;
    }
}
