package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class CCDCase {

    private String referenceNumber;
    private String submitterId;
    private LocalDateTime submittedOn;
    private String externalId;
    private LocalDate issuedOn;
    private String submitterEmail;
    private CCDClaim claim;

}
