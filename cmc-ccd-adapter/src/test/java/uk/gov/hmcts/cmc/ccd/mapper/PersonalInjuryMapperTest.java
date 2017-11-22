package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDPersonalInjury;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;

import static org.assertj.core.api.Assertions.assertThat;


public class PersonalInjuryMapperTest {

    PersonalInjuryMapper mapper = new PersonalInjuryMapper();

    @Test
    public void shouldMapPersonalInjuryToCCD() {
        //given
        PersonalInjury personalInjury = new PersonalInjury(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);

        //when
        CCDPersonalInjury ccdPersonalInjury = mapper.to(personalInjury);

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
        PersonalInjury personalInjury = mapper.from(ccdPersonalInjury);

        assertThat(personalInjury.getGeneralDamages()).isEqualTo(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);
    }

}
