package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Optional;

@SuppressWarnings("squid:S1610")
public abstract class TheirDetailsMixIn {

    @JsonProperty("claimantProvidedName")
    abstract String getName();

    @JsonProperty("claimantProvidedAddress")
    abstract Address getAddress();

    @JsonProperty("claimantProvidedEmail")
    abstract Optional<String> getEmail();

    @JsonProperty("claimantProvidedMobileName")
    abstract String getMobilePhone();

    @JsonUnwrapped
    abstract Optional<Representative> getRepresentative();

    @JsonProperty("claimantProvidedServiceAddress")
    abstract Optional<Address> getServiceAddress();
}
