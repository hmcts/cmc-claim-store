package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import java.util.Optional;

@SuppressWarnings("squid:S1610")
public abstract class RepresentativeMixIn {

    @JsonProperty("OrganisationName")
    abstract String getOrganisationName();

    @JsonProperty("OrganisationAddress")
    abstract Address getOrganisationAddress();

    @JsonUnwrapped(prefix = "claimantProvided")
    abstract Optional<ContactDetails> getOrganisationContactDetails();
}
