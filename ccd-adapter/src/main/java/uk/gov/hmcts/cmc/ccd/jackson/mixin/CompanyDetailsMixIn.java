package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface CompanyDetailsMixIn extends TheirDetailsMixIn {

    @JsonProperty("claimantProvidedContactPerson")
    String getContactPerson();
}
