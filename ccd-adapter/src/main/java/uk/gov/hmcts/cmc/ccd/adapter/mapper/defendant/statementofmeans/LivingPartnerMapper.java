package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDisabilityStatus;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.ccd.adapter.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

@Component
public class LivingPartnerMapper implements Mapper<CCDLivingPartner, LivingPartner> {

    @Override
    public CCDLivingPartner to(LivingPartner livingPartner) {

        return CCDLivingPartner.builder()
            .disability(CCDDisabilityStatus.valueOf(livingPartner.getDisability().name()))
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
            .disability(DisabilityStatus.valueOf(ccdLivingPartner.getDisability().name()))
            .over18(ccdLivingPartner.getOver18() != null && ccdLivingPartner.getOver18().toBoolean())
            .pensioner(ccdLivingPartner.getPensioner() != null && ccdLivingPartner.getPensioner().toBoolean())
            .build();
    }
}
