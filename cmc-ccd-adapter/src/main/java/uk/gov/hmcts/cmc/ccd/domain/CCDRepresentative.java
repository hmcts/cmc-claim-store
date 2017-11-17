package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDRepresentative {

    private final String organisationName;
    private final CCDAddress organisationAddress;
    private final CCDContactDetails organisationContactDetails;
}
