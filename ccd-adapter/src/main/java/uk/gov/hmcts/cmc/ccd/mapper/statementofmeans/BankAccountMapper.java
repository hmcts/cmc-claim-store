package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount.BankAccountType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

@Component
public class BankAccountMapper implements Mapper<CCDBankAccount, BankAccount> {

    @Override
    public CCDBankAccount to(BankAccount bankAccount) {
        return CCDBankAccount.builder()
            .type(BankAccountType.valueOf(bankAccount.getType().name()))
            .joint(bankAccount.isJoint() ? YES : NO)
            .balance(bankAccount.getBalance())
            .build();
    }

    @Override
    public BankAccount from(CCDBankAccount ccdBankAccount) {
        return new BankAccount(
            BankAccount.BankAccountType.valueOf(ccdBankAccount.getType().name()),
            ccdBankAccount.getJoint() == YES ? true : false,
            ccdBankAccount.getBalance()
        );
    }
}
