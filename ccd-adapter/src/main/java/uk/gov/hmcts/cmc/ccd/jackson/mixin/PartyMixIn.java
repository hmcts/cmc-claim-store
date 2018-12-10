package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.time.LocalDate;

@SuppressWarnings("squid:S1610")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = Individual.class, name = "individual"),
        @JsonSubTypes.Type(value = SoleTrader.class, name = "soleTrader"),
        @JsonSubTypes.Type(value = Company.class, name = "company"),
        @JsonSubTypes.Type(value = Organisation.class, name = "organisation")
    }
)
public abstract class PartyMixIn {

    @JsonProperty("->type")
    abstract String getType();

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
