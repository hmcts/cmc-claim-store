package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.valueOf;

@Component
public class BankAccountMapper implements Mapper<CCDBankAccount, BankAccount> {

    @Override
    public CCDBankAccount to(BankAccount bankAccount) {
        return CCDBankAccount.builder()
            .type(bankAccount.getType())
            .joint(valueOf(bankAccount.isJoint()))
            .balance(bankAccount.getBalance())
            .build();
    }

    @Override
    public BankAccount from(CCDBankAccount ccdBankAccount) {
        return new BankAccount(
            ccdBankAccount.getType(),
            ccdBankAccount.getJoint() != null && ccdBankAccount.getJoint().toBoolean(),
            ccdBankAccount.getBalance()
        );
    }
}
