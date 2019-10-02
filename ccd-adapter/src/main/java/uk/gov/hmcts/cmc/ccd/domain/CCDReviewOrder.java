package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CCDReviewOrder {

    public enum RequestedBy { CLAIMANT, DEFENDANT }

    private String reason;
    private RequestedBy requestedBy;
    private LocalDateTime requestedAt;
}
