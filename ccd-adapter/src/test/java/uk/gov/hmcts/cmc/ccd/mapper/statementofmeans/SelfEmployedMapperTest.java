package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDSelfEmployed;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployed;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SelfEmployedMapperTest {

    @Autowired
    private SelfEmployedMapper mapper;

    @Test
    public void shouldMapSelfEmployedToCCD() {
        //given
        SelfEmployed selfEmployed = SelfEmployed.builder()
            .jobTitle("My job")
            .annualTurnover(BigDecimal.TEN)
            .behindOnTaxPayments(YesNoOption.YES)
            .reason("my reason")
            .amountYouOwe(BigDecimal.ONE)
            .build();

        //when
        CCDSelfEmployed ccdSelfEmployed = mapper.to(selfEmployed);

        //then
        assertThat(selfEmployed).isEqualTo(ccdSelfEmployed);

    }

    @Test
    public void shouldMapEmployerFromCCD() {
        //given
        CCDSelfEmployed ccdSelfEmployed = CCDSelfEmployed.builder()
            .jobTitle("My job")
            .annualTurnover(BigDecimal.TEN)
            .behindOnTaxPayments(CCDYesNoOption.YES)
            .reason("my reason")
            .amountYouOwe(BigDecimal.ONE)
            .build();

        //when
        SelfEmployed selfEmployed = mapper.from(ccdSelfEmployed);

        //then
        assertThat(selfEmployed).isEqualTo(ccdSelfEmployed);
    }
}
