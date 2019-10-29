package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class CCDDirectionOrder {

    private LocalDateTime createdOn;
    private String hearingCourtName;
    private CCDAddress hearingCourtAddress;
}
