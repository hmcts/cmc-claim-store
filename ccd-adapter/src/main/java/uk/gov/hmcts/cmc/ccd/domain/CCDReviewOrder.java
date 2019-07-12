package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CCDReviewOrder {

    public enum PartyType { CLAIMANT, DEFENDANT }

    private String reason;
    private PartyType requestedBy;
    private LocalDateTime requestedAt;
}
