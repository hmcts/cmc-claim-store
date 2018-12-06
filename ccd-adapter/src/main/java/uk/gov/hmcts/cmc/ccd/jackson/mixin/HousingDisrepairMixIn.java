package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

import java.util.Optional;

public interface HousingDisrepairMixIn {

    @JsonProperty("housingDisrepairCostOfRepairDamages")
    DamagesExpectation getCostOfRepairsDamages();

    @JsonProperty("housingDisrepairOtherDamages")
    Optional<DamagesExpectation> getOtherDamages();
}
