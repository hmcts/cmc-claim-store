package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CompanyDetailsMixIn extends TheirDetailsMixIn {

    @JsonProperty("claimantProvidedContactPerson")
    abstract String getContactPerson();
}
