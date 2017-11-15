package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CcdCase {

    private final String referenceNumber;
    private final String submitterId;
    private final LocalDateTime submittedOn;
    private final String externalId;
    private final LocalDate issuedOn;
    private final String submitterEmail;
    private final ClaimData claimData;
    
}
