package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;

@Component
public class HousingDisrepairMapper implements BuilderMapper<CCDCase, HousingDisrepair, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(HousingDisrepair housingDisrepair, CCDCase.CCDCaseBuilder builder) {
        builder.housingDisrepairCostOfRepairDamages(housingDisrepair.getCostOfRepairsDamages().name());
        housingDisrepair.getOtherDamages().ifPresent(damage -> builder.housingDisrepairOtherDamages(damage.name()));
    }

    @Override
    public HousingDisrepair from(CCDCase ccdCase) {
        if (ccdCase == null) {
            return null;
        }

        DamagesExpectation costOfRepairs = DamagesExpectation.valueOf(ccdCase.getHousingDisrepairCostOfRepairDamages());
        String ccdOtherDamages = ccdCase.getHousingDisrepairOtherDamages();
        DamagesExpectation otherDamages = ccdOtherDamages != null ? DamagesExpectation.valueOf(ccdOtherDamages) : null;
        return new HousingDisrepair(costOfRepairs, otherDamages);
    }
}
