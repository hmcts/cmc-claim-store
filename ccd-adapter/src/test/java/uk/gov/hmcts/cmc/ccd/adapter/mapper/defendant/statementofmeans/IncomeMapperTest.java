package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncomeType.OTHER;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.JOB;
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
        CCDCollectionElement<CCDIncome> ccdIncome = mapper.to(income);

        //then
        assertThat(income).isEqualTo(ccdIncome.getValue());
        assertThat(income.getId()).isEqualTo(ccdIncome.getId());
    }

    @Test
    public void shouldMapIncomeFromCCD() {
        //given
        CCDIncome ccdIncome = CCDIncome.builder()
            .type(OTHER)
            .frequency(CCDPaymentFrequency.MONTH)
            .amountReceived("1000")
            .otherSource("Trading")
            .build();
        String collectionId = UUID.randomUUID().toString();

        //when
        Income income = mapper.from(CCDCollectionElement.<CCDIncome>builder()
            .id(collectionId)
            .value(ccdIncome)
            .build());

        //then
        assertThat(income).isEqualTo(ccdIncome);
        assertThat(income.getId()).isEqualTo(collectionId);
    }
}
