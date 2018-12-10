package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

@SuppressWarnings("squid:S1610")
public abstract class PersonalInjuryMixIn {

    //    @JsonProperty("personalInjuryGeneralDamages")
    abstract DamagesExpectation getGeneralDamages();
}
