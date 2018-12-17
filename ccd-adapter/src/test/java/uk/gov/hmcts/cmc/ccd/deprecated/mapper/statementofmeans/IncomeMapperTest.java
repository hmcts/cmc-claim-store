package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.JOB;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.OTHER;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IncomeMapperTest {

    @Autowired
    private IncomeMapper mapper;

    @Test
    public void shouldMapIncomeToCCD() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .amount(BigDecimal.TEN)
            .build();

        //when
        CCDIncome ccdIncome = mapper.to(income);

        //then
        assertThat(income).isEqualTo(ccdIncome);
    }

    @Test
    public void shouldMapIncomeFromCCD() {
        //given
        CCDIncome ccdIncome = CCDIncome.builder()
            .type(OTHER)
            .frequency(MONTH)
            .amountReceived(BigDecimal.TEN)
            .otherSource("Trading")
            .build();

        //when
        Income income = mapper.from(ccdIncome);

        //then
        assertThat(income).isEqualTo(ccdIncome);
    }
}
