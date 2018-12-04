package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;

public abstract class PartyMixIn {
    @JsonProperty("partyDateOfBirth")
    abstract LocalDate getDateOfBirth();

    @JsonProperty("partyName")
    abstract String getName();

    @JsonProperty("partyAddress")
    abstract Address getAddress();

    @JsonProperty("partyEmail")
    abstract String getEmail();

    @JsonProperty("partyCorrespondenceAddress")
    abstract Address getCorrespondenceAddress();

    @JsonProperty("partyMobileName")
    abstract String getMobilePhone();

    @JsonUnwrapped
    abstract Representative getRepresentative();

    @JsonProperty("partyServiceAddress")
    abstract Address getServiceAddress();
}
