package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Representative {

    private final String organisationName;
    private final Address organisationAddress;
    private final ContactDetails organisationContactDetails;
}
