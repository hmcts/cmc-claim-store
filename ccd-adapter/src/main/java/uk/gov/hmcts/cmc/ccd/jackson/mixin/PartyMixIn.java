package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;

@SuppressWarnings("squid:S1610")
public interface PartyMixIn {
    @JsonProperty("partyDateOfBirth")
    LocalDate getDateOfBirth();

    @JsonProperty("partyName")
    String getName();

    @JsonProperty("partyAddress")
    Address getAddress();

    @JsonProperty("partyEmail")
    String getEmail();

    @JsonProperty("partyCorrespondenceAddress")
    Address getCorrespondenceAddress();

    @JsonProperty("partyMobileName")
    String getMobilePhone();

    @JsonUnwrapped
    Representative getRepresentative();

    @JsonProperty("partyServiceAddress")
    Address getServiceAddress();
}
