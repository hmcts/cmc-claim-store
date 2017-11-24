package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private String submitterEmail;
    private CCDClaim claim;

}
