package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Optional;

public interface TheirDetailsMixIn {

    @JsonProperty("claimantProvidedName")
    String getName();

    @JsonProperty("claimantProvidedAddress")
    Address getAddress();

    @JsonProperty("claimantProvidedEmail")
    Optional<String> getEmail();

    @JsonProperty("claimantProvidedMobileName")
    String getMobilePhone();

    @JsonUnwrapped
    Optional<Representative> getRepresentative();

    @JsonProperty("claimantProvidedServiceAddress")
    Optional<Address> getServiceAddress();
}
