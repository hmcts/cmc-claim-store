package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

import java.util.Optional;

public abstract class HousingDisrepairMixIn {

    @JsonProperty("housingDisrepairCostOfRepairDamages")
    abstract DamagesExpectation getCostOfRepairsDamages();

    @JsonProperty("housingDisrepairOtherDamages")
    abstract Optional<DamagesExpectation> getOtherDamages();
}
