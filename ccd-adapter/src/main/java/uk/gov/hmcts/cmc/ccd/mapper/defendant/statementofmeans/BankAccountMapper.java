package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.valueOf;

@Component
public class BankAccountMapper {

    public CCDCollectionElement<CCDBankAccount> to(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        return CCDCollectionElement.<CCDBankAccount>builder()
            .value(CCDBankAccount.builder()
                .type(bankAccount.getType())
                .joint(valueOf(bankAccount.isJoint()))
                .balance(bankAccount.getBalance())
                .build())
            .id(bankAccount.getId())
            .build();
    }

    public BankAccount from(CCDCollectionElement<CCDBankAccount> ccdBankAccount) {
        CCDBankAccount value = ccdBankAccount.getValue();
        if (value == null) {
            return null;
        }

        return BankAccount.builder()
            .id(ccdBankAccount.getId())
            .type(value.getType())
            .joint(value.getJoint() != null && value.getJoint().toBoolean())
            .balance(value.getBalance())
            .build();
    }
}
