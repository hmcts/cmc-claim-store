package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDisabilityStatus;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus.NO;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class LivingPartnerMapperTest {
    @Autowired
    private LivingPartnerMapper mapper;

    @Test
    public void shouldMapLivingPartnerToCCD() {
        //given
        LivingPartner livingPartner = LivingPartner.builder()
            .disability(NO)
            .over18(true)
            .pensioner(false)
            .build();

        //when
        CCDLivingPartner ccdLivingPartner = mapper.to(livingPartner);

        //then
        assertThat(livingPartner).isEqualTo(ccdLivingPartner);
    }

    @Test
    public void shouldMapLivingPartnerFromCCD() {
        //given
        CCDLivingPartner ccdLivingPartner = CCDLivingPartner.builder()
            .disability(CCDDisabilityStatus.NO)
            .over18(CCDYesNoOption.YES)
            .pensioner(CCDYesNoOption.NO)
            .build();

        //when
        LivingPartner livingPartner = mapper.from(ccdLivingPartner);

        //then
        assertThat(livingPartner).isEqualTo(ccdLivingPartner);
    }
}
