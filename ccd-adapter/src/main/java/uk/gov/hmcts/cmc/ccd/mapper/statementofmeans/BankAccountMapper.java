package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

public class BankAccountMapper implements Mapper<CCDBankAccount, BankAccount> {

    @Override
    public CCDBankAccount to(BankAccount bankAccount) {
        return null;
    }

    @Override
    public BankAccount from(CCDBankAccount ccdBankAccount) {
        return null;
    }
}
