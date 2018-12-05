package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

@SuppressWarnings({"squid:S1610", "squid:S00112"})
public abstract class TheirDetailsMixIn {

    @JsonProperty("claimantProvidedName")
    abstract String getName();

    @JsonProperty("claimantProvidedAddress")
    abstract Address getAddress();

    @JsonProperty("claimantProvidedEmail")
    abstract String getEmail();

    @JsonProperty("claimantProvidedCorrespondenceAddress")
    abstract Address getCorrespondenceAddress();

    @JsonProperty("claimantProvidedMobileName")
    abstract String getMobilePhone();

    @JsonUnwrapped
    abstract Representative getRepresentative();

    @JsonProperty("claimantProvidedServiceAddress")
    abstract Address getServiceAddress();
}
