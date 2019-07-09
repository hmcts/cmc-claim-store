package uk.gov.hmcts.cmc.ccd-adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

public class BankAccountAssert extends AbstractAssert<BankAccountAssert, BankAccount> {

    public BankAccountAssert(BankAccount actual) {
        super(actual, BankAccountAssert.class);
    }

    public BankAccountAssert isEqualTo(CCDBankAccount ccdBankAccount) {
        isNotNull();

        assertMoney(actual.getBalance())
            .isEqualTo(
                ccdBankAccount.getBalance(),
                format("Expected BankAccount.balance to be <%s> but was <%s>",
                    ccdBankAccount.getBalance(), actual.getBalance()
                )
            );

        if (actual.isJoint() && ccdBankAccount.getJoint() == NO) {
            failWithMessage("Expected BankAccount.joint to be <%s> but was <%s>",
                ccdBankAccount.getJoint().name(), actual.isJoint());
        }

        if (!actual.isJoint() && ccdBankAccount.getJoint() == YES) {
            failWithMessage("Expected BankAccount.joint to be <%s> but was <%s>",
                ccdBankAccount.getJoint().name(), actual.isJoint());
        }

        if (!Objects.equals(actual.getType().name(), ccdBankAccount.getType().name())) {
            failWithMessage("Expected BankAccount.type to be <%s> but was <%s>",
                ccdBankAccount.getType(), actual.getType());
        }
        return this;
    }
}
