package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CCDCase {

    private final String referenceNumber;
    private final String submitterId;
    private final LocalDateTime submittedOn;
    private final String externalId;
    private final LocalDate issuedOn;
    private final String submitterEmail;
    private final CCDClaim claim;

}
