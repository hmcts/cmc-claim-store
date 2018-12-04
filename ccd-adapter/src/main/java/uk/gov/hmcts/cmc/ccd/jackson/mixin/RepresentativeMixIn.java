package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

public abstract class RepresentativeMixIn {

    @JsonProperty("representativeOrganisationName")
    abstract String getOrganisationName();

    @JsonProperty("representativeOrganisationAddress")
    abstract Address getOrganisationAddress();

    @JsonUnwrapped(prefix = "representativeOrganisation")
    abstract ContactDetails getOrganisationContactDetails();
}
