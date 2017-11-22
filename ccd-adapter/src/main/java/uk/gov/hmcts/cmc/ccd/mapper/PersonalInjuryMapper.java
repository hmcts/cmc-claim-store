package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPersonalInjury;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;

@Component
public class PersonalInjuryMapper implements Mapper<CCDPersonalInjury, PersonalInjury> {

    @Override
    public CCDPersonalInjury to(PersonalInjury personalInjury) {
        return CCDPersonalInjury.builder()
            .generalDamages(personalInjury.getGeneralDamages().name())
            .build();
    }

    @Override
    public PersonalInjury from(CCDPersonalInjury ccdPersonalInjury) {
        return new PersonalInjury(DamagesExpectation.valueOf(ccdPersonalInjury.getGeneralDamages()));
    }
}
