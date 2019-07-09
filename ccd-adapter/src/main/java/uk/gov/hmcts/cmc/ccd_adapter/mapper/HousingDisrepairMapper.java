package uk.gov.hmcts.cmc.ccd_adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class HousingDisrepairMapper implements uk.gov.hmcts.cmc.ccd_adapter.mapper.BuilderMapper<CCDCase, HousingDisrepair, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(HousingDisrepair housingDisrepair, CCDCase.CCDCaseBuilder builder) {
        builder.housingDisrepairCostOfRepairDamages(housingDisrepair.getCostOfRepairsDamages().name());
        housingDisrepair.getOtherDamages().ifPresent(damage -> builder.housingDisrepairOtherDamages(damage.name()));
    }

    @Override
    public HousingDisrepair from(CCDCase ccdCase) {
        if (isBlank(ccdCase.getHousingDisrepairCostOfRepairDamages())
            && isBlank(ccdCase.getHousingDisrepairOtherDamages())
        ) {
            return null;
        }

        DamagesExpectation costOfRepairs = DamagesExpectation.valueOf(ccdCase.getHousingDisrepairCostOfRepairDamages());
        String ccdOtherDamages = ccdCase.getHousingDisrepairOtherDamages();
        DamagesExpectation otherDamages = ccdOtherDamages != null ? DamagesExpectation.valueOf(ccdOtherDamages) : null;
        return new HousingDisrepair(costOfRepairs, otherDamages);
    }
}
