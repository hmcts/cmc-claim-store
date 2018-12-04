package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

public abstract class PersonalInjuryMixIn {

    @JsonProperty("personalInjuryGeneralDamages")
    abstract DamagesExpectation getGeneralDamages();
}
