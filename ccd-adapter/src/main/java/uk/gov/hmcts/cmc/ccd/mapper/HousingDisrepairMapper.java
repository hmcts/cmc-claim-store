package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDHousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;

@Component
public class HousingDisrepairMapper implements Mapper<CCDHousingDisrepair, HousingDisrepair> {

    @Override
    public CCDHousingDisrepair to(HousingDisrepair housingDisrepair) {
        CCDHousingDisrepair.CCDHousingDisrepairBuilder builder = CCDHousingDisrepair.builder()
            .costOfRepairsDamages(housingDisrepair.getCostOfRepairsDamages().name());
        housingDisrepair.getOtherDamages().ifPresent(damage -> builder.otherDamages(damage.name()));
        return builder.build();
    }

    @Override
    public HousingDisrepair from(CCDHousingDisrepair ccdHousingDisrepair) {
        if (ccdHousingDisrepair == null) {
            return null;
        }

        DamagesExpectation costOfRepairs = DamagesExpectation.valueOf(ccdHousingDisrepair.getCostOfRepairsDamages());
        String ccdOtherDamages = ccdHousingDisrepair.getOtherDamages();
        DamagesExpectation otherDamages = ccdOtherDamages != null ? DamagesExpectation.valueOf(ccdOtherDamages) : null;
        return new HousingDisrepair(costOfRepairs, otherDamages);
    }
}
