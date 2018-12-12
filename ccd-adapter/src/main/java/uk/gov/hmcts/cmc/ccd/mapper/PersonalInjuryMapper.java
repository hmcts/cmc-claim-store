package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;

@Component
public class PersonalInjuryMapper implements BuilderMapper<CCDCase, PersonalInjury, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(PersonalInjury personalInjury, CCDCase.CCDCaseBuilder builder) {
        builder.personalInjuryGeneralDamages(personalInjury.getGeneralDamages().name());
    }

    @Override
    public PersonalInjury from(CCDCase ccdCase) {
        if (ccdCase == null) {
            return null;
        }

        return new PersonalInjury(DamagesExpectation.valueOf(ccdCase.getPersonalInjuryGeneralDamages()));
    }
}
