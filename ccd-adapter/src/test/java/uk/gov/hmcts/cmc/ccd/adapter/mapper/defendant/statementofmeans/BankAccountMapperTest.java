package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccountType.SAVINGS_ACCOUNT;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.CURRENT_ACCOUNT;

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
        assertThat(bankAccount.getId()).isEqualTo(ccdBankAccount.getId());
        assertThat(bankAccount).isEqualTo(ccdBankAccount.getValue());
    }

    @Test
    public void shouldMapBankAccountFromCCD() {
        //given
        CCDBankAccount ccdBankAccount = CCDBankAccount.builder()
            .balance("10000")
            .joint(CCDYesNoOption.NO)
            .type(SAVINGS_ACCOUNT)
            .build();

        String collectionId = UUID.randomUUID().toString();

        //when
        BankAccount bankAccount = mapper.from(CCDCollectionElement.<CCDBankAccount>builder()
            .id(collectionId)
            .value(ccdBankAccount)
            .build());

        //then
        assertThat(bankAccount).isEqualTo(ccdBankAccount);
        assertThat(bankAccount.getId()).isEqualTo(collectionId);
    }
}
