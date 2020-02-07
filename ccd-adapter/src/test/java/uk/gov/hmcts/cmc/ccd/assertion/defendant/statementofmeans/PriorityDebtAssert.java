package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class PriorityDebtAssert extends CustomAssert<PriorityDebtAssert, PriorityDebt> {

    public PriorityDebtAssert(PriorityDebt actual) {
        super("PriorityDebt", actual, PriorityDebtAssert.class);
    }

    public PriorityDebtAssert isEqualTo(CCDPriorityDebt expected) {
        isNotNull();

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        compare("frequency",
            expected.getFrequency(), Enum::name,
            Optional.ofNullable(actual.getFrequency()).map(Enum::name));

        compare("amount",
            expected.getAmount(),
            Optional.ofNullable(actual.getAmount()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        return this;
    }
}
