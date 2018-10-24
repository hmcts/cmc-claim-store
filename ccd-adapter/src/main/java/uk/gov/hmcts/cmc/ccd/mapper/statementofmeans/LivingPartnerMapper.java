package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

@Component
public class LivingPartnerMapper implements Mapper<CCDLivingPartner, LivingPartner> {

    @Override
    public CCDLivingPartner to(LivingPartner livingPartner) {

        return CCDLivingPartner.builder()
            .disability(livingPartner.getDisability())
            .over18(CCDYesNoOption.valueOf(livingPartner.isOver18()))
            .pensioner(CCDYesNoOption.valueOf(livingPartner.isPensioner()))
            .build();
    }

    @Override
    public LivingPartner from(CCDLivingPartner ccdLivingPartner) {
        if (ccdLivingPartner == null) {
            return null;
        }

        return LivingPartner.builder()
            .disability(ccdLivingPartner.getDisability())
            .over18(ccdLivingPartner.getOver18().toBoolean())
            .pensioner(ccdLivingPartner.getPensioner().toBoolean())
            .build();
    }
}
