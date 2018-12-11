package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class CCDRepresentative {

    private String organisationName;
    private CCDAddress organisationAddress;
    private CCDContactDetails organisationContactDetails;

    public Optional<CCDContactDetails> getOrganisationContactDetails() {
        return Optional.ofNullable(organisationContactDetails);
    }
}
