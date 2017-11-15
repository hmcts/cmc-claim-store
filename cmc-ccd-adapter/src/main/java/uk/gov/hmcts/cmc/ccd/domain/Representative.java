package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class Representative {

    private final String organisationName;
    private final Address organisationAddress;
    private final ContactDetails organisationContactDetails;
}
