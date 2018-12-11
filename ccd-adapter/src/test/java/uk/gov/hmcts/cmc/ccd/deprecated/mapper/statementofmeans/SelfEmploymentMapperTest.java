package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDOnTaxPayments;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDSelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;

import static java.math.BigDecimal.TEN;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SelfEmploymentMapperTest {

    @Autowired
    private SelfEmploymentMapper mapper;

    @Test
    public void shouldMapSelfEmployedToCCD() {
        //given
        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("My job")
            .annualTurnover(TEN)
            .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Any reason").build())
            .build();

        //when
        CCDSelfEmployment ccdSelfEmployment = mapper.to(selfEmployment);

        //then
        assertThat(selfEmployment).isEqualTo(ccdSelfEmployment);

    }

    @Test
    public void shouldMapEmployerFromCCD() {
        //given
        CCDSelfEmployment ccdSelfEmployed = CCDSelfEmployment.builder()
            .jobTitle("My job")
            .annualTurnover(TEN)
            .onTaxPayments(CCDOnTaxPayments.builder().amountYouOwe(TEN).reason("My reason").build())
            .build();

        //when
        SelfEmployment selfEmployment = mapper.from(ccdSelfEmployed);

        //then
        assertThat(selfEmployment).isEqualTo(ccdSelfEmployed);
    }
}
