package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;

public class SampleReviewOrder {

    private SampleReviewOrder() {
    }

    public static ReviewOrder.ReviewOrderBuilder builder() {
        return ReviewOrder.builder()
            .requestedAt(LocalDateTime.now())
            .requestedBy(CLAIMANT)
            .reason("My valid reason.");
    }

    public static ReviewOrder getDefault() {
        return builder().build();
    }
}
