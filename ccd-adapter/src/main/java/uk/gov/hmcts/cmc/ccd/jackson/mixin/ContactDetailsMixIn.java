package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@SuppressWarnings("squid:S1610")
public abstract class ContactDetailsMixIn {
    @JsonProperty("representativeOrganisationPhone")
    abstract Optional<String> getPhone();

    @JsonProperty("representativeOrganisationEmail")
    abstract Optional<String> getEmail();

    @JsonProperty("representativeOrganisationDxAddress")
    abstract Optional<String> getDxAddress();
}
