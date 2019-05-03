package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Supplier;

public class SampleClaimSubmissionOperationIndicators {

    public static Supplier<ClaimSubmissionOperationIndicators> withPinOperationSuccess =
        () -> ClaimSubmissionOperationIndicators
            .builder()
            .defendantPinLetterUpload(YesNoOption.YES)
            .bulkPrint(YesNoOption.YES)
            .staffNotification(YesNoOption.YES)
            .defendantNotification(YesNoOption.YES)
            .build();

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
