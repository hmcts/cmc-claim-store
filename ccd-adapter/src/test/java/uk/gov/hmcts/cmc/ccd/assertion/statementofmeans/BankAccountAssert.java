package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.util.Objects;

public class BankAccountAssert extends AbstractAssert<BankAccountAssert, BankAccount> {

    public BankAccountAssert(BankAccount actual) {
        super(actual, BankAccountAssert.class);
    }

    public BankAccountAssert isEqualTo(CCDBankAccount ccdBankAccount) {
        isNotNull();

        if (!Objects.equals(actual.getBalance(), ccdBankAccount.getBalance())) {
            failWithMessage("Expected BankAccount.balance to be <%s> but was <%s>",
                ccdBankAccount.getBalance(), actual.getBalance());
        }

        if (!Objects.equals(actual.getIsJoint().name(), ccdBankAccount.getIsJoint().name())) {
            failWithMessage("Expected BankAccount.isJoint to be <%s> but was <%s>",
                ccdBankAccount.getIsJoint().name(), actual.getIsJoint().name());
        }

        if (!Objects.equals(actual.getTypeOfAccount().name(), ccdBankAccount.getTypeOfAccount().name())) {
            failWithMessage("Expected Address.line3 to be <%s> but was <%s>",
                ccdBankAccount.getTypeOfAccount().name(), actual.getTypeOfAccount().name());
        }

        return this;
    }

}
