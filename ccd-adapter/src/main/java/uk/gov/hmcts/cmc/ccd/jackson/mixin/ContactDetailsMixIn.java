package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public interface ContactDetailsMixIn {
    @JsonProperty("representativeOrganisationPhone")
    Optional<String> getPhone();

    @JsonProperty("representativeOrganisationEmail")
    Optional<String> getEmail();

    @JsonProperty("representativeOrganisationDxAddress")
    Optional<String> getDxAddress();
}
