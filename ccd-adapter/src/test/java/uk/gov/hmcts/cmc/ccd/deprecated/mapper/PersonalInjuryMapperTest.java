package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPersonalInjury;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PersonalInjuryMapperTest {

    @Autowired
    private PersonalInjuryMapper personalInjuryMapper;

    @Test
    public void shouldMapPersonalInjuryToCCD() {
        //given
        PersonalInjury personalInjury = new PersonalInjury(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);

        //when
        CCDPersonalInjury ccdPersonalInjury = personalInjuryMapper.to(personalInjury);

        //then
        assertThat(ccdPersonalInjury.getGeneralDamages()).isEqualTo(personalInjury.getGeneralDamages().name());
    }

    @Test
    public void shouldMapPersonalInjuryFromCCD() {
        //given
        CCDPersonalInjury ccdPersonalInjury = CCDPersonalInjury.builder()
            .generalDamages(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS.name())
            .build();

        //when
        PersonalInjury personalInjury = personalInjuryMapper.from(ccdPersonalInjury);

        assertThat(personalInjury.getGeneralDamages()).isEqualTo(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);
    }

}
