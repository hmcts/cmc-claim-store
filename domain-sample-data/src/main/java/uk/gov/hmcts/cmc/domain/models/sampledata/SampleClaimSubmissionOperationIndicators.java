package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

public class SampleClaimSubmissionOperationIndicators {

    private ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder builder =
        ClaimSubmissionOperationIndicators.builder();

    public SampleClaimSubmissionOperationIndicators withClaimantNotification() {
        builder.claimantNotification(true);
        return this;
    }

    public ClaimSubmissionOperationIndicators build() {
        return builder.build();
    }
}
