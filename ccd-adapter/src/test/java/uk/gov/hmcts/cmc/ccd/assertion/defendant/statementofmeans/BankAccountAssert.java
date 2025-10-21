package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccountType;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class BankAccountAssert extends CustomAssert<BankAccountAssert, BankAccount> {

    public BankAccountAssert(BankAccount actual) {
        super("BankAccount", actual, BankAccountAssert.class);
    }

    public BankAccountAssert isEqualTo(CCDBankAccount expected) {
        isNotNull();

        compare("balance",
            expected.getBalance(),
            Optional.ofNullable(actual.getBalance()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("joint",
            expected.getJoint(), CCDYesNoOption::toBoolean,
            Optional.of(actual.isJoint()));

        compare("type",
            expected.getType(), CCDBankAccountType::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        return this;
    }
}
