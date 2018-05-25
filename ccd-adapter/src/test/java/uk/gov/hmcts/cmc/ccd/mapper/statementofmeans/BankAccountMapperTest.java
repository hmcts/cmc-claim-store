package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

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
            .isJoint(YesNoOption.NO)
            .typeOfAccount(BankAccount.BankAccountType.CURRENT_ACCOUNT)
            .build();

        //when
        CCDBankAccount ccdBankAccount = mapper.to(bankAccount);

        //then
        assertThat(bankAccount).isEqualTo(ccdBankAccount);
    }

    @Test
    public void shouldMapBankAccountFromCCD() {
        //given
        CCDBankAccount ccdBankAccount = CCDBankAccount.builder()
            .balance(BigDecimal.valueOf(100))
            .isJoint(CCDYesNoOption.NO)
            .typeOfAccount(CCDBankAccount.BankAccountType.SAVING_ACCOUNT)
            .build();

        //when
        BankAccount bankAccount = mapper.from(ccdBankAccount);

        //then
        assertThat(bankAccount).isEqualTo(ccdBankAccount);
    }
}
