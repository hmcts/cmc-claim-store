package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DebtMapperTest {

    @Autowired
    private DebtMapper mapper;

    @Test
    public void shouldMapDebtToCCD() {
        //given
        Debt debt = Debt.builder()
            .totalOwed(BigDecimal.TEN)
            .description("Reference")
            .monthlyPayments(BigDecimal.ONE)
            .build();

        //when
        CCDDebt ccdDebt = mapper.to(debt);

        //then
        assertThat(debt).isEqualTo(ccdDebt);

    }

    @Test
    public void shouldMapDebtFromCCD() {
        //given
        CCDDebt ccdDebt = CCDDebt.builder()
            .totalOwed(BigDecimal.TEN)
            .description("Reference")
            .monthlyPayments(BigDecimal.ONE)
            .build();

        //when
        Debt debt = mapper.from(CCDCollectionElement.<CCDDebt>builder().value(ccdDebt).build());

        //then
        assertThat(debt).isEqualTo(ccdDebt);
    }
}
