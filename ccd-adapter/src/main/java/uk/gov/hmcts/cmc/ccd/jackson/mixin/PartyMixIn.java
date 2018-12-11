package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.PartyType;

import java.time.LocalDate;

@SuppressWarnings("squid:S1610")
public abstract class PartyMixIn {

    @JsonProperty("Type")
    abstract PartyType getType();

    @JsonProperty("DateOfBirth")
    abstract LocalDate getDateOfBirth();

    @JsonProperty("Name")
    abstract String getName();

    @JsonProperty("Address")
    abstract Address getAddress();

    @JsonProperty("Email")
    abstract String getEmail();

    @JsonProperty("CorrespondenceAddress")
    abstract Address getCorrespondenceAddress();

    @JsonProperty("MobileName")
    abstract String getMobilePhone();

    @JsonUnwrapped(prefix = "Representative")
    abstract Representative getRepresentative();

    @JsonProperty("ServiceAddress")
    abstract Address getServiceAddress();
}
