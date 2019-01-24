package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.CURRENT_ACCOUNT;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.SAVINGS_ACCOUNT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class BankAccountMapperTest {

    @Autowired
    private BankAccountMapper mapper;

    @Test
    public void shouldMapBankAccountToCCD() {
        //given
        BankAccount bankAccount = BankAccount.builder()
            .balance(BigDecimal.valueOf(100))
            .joint(false)
            .type(CURRENT_ACCOUNT)
            .build();

        //when
        CCDCollectionElement<CCDBankAccount> ccdBankAccount = mapper.to(bankAccount);

        //then
        Assertions.assertThat(bankAccount.getId()).isEqualTo(ccdBankAccount.getId());
        assertThat(bankAccount).isEqualTo(ccdBankAccount.getValue());
    }

    @Test
    public void shouldMapBankAccountFromCCD() {
        //given
        CCDBankAccount ccdBankAccount = CCDBankAccount.builder()
            .balance(BigDecimal.valueOf(100))
            .joint(CCDYesNoOption.NO)
            .type(SAVINGS_ACCOUNT)
            .build();

        //when
        BankAccount bankAccount = mapper.from(CCDCollectionElement.<CCDBankAccount>builder()
            .value(ccdBankAccount)
            .build());

        //then
        assertThat(bankAccount).isEqualTo(ccdBankAccount);
    }
}
