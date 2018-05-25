package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount.BankAccountType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

@Component
public class BankAccountMapper implements Mapper<CCDBankAccount, BankAccount> {

    @Override
    public CCDBankAccount to(BankAccount bankAccount) {
        return CCDBankAccount.builder()
            .typeOfAccount(BankAccountType.valueOf(bankAccount.getTypeOfAccount().name()))
            .isJoint(CCDYesNoOption.valueOf(bankAccount.getIsJoint().name()))
            .balance(bankAccount.getBalance())
            .build();
    }

    @Override
    public BankAccount from(CCDBankAccount ccdBankAccount) {
        return new BankAccount(
            BankAccount.BankAccountType.valueOf(ccdBankAccount.getTypeOfAccount().name()),
            YesNoOption.valueOf(ccdBankAccount.getIsJoint().name()),
            ccdBankAccount.getBalance()
        );
    }
}
