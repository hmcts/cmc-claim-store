package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccountType;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.valueOf;

@Component
public class BankAccountMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public BankAccountMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    public CCDCollectionElement<CCDBankAccount> to(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        return CCDCollectionElement.<CCDBankAccount>builder()
            .value(CCDBankAccount.builder()
                .type(CCDBankAccountType.valueOf(bankAccount.getType().name()))
                .joint(valueOf(bankAccount.isJoint()))
                .balance(moneyMapper.to(bankAccount.getBalance()))
                .build())
            .id(bankAccount.getId())
            .build();
    }

    public BankAccount from(CCDCollectionElement<CCDBankAccount> collectionElement) {
        CCDBankAccount ccdBankAccount = collectionElement.getValue();
        
        if (ccdBankAccount == null) {
            return null;
        }

        return BankAccount.builder()
            .id(collectionElement.getId())
            .type(BankAccount.BankAccountType.valueOf(ccdBankAccount.getType().name()))
            .joint(ccdBankAccount.getJoint() != null && ccdBankAccount.getJoint().toBoolean())
            .balance(moneyMapper.from(ccdBankAccount.getBalance()))
            .build();
    }
}
