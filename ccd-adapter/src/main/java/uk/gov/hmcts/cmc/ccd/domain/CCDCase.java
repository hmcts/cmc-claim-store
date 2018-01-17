package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private String submitterEmail;
    private CCDClaim claimData;

}
