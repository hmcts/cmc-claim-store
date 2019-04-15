package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

public class SampleClaimSubmissionOperationIndicators {

    private ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder builder =
        ClaimSubmissionOperationIndicators.builder();

    public SampleClaimSubmissionOperationIndicators withClaimantNotification() {
        builder.claimantNotification(YesNoOption.YES);
        return this;
    }

    public ClaimSubmissionOperationIndicators build() {
        return builder.build();
    }
}
