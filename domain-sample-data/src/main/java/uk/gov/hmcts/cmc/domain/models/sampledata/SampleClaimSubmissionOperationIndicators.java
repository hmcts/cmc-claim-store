package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import java.util.function.Supplier;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class SampleClaimSubmissionOperationIndicators {

    public static final Supplier<ClaimSubmissionOperationIndicators> withPinOperationSuccess =
        () -> ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(YES)
            .staffNotification(YES)
            .defendantNotification(YES)
            .build();

    public static final Supplier<ClaimSubmissionOperationIndicators> withOnePinOperationFailure =
        () -> withPinOperationSuccess.get().toBuilder()
            .bulkPrint(NO)
            .build();

    public static final Supplier<ClaimSubmissionOperationIndicators> withSealedClaimUploadOperationSuccess =
        () -> withPinOperationSuccess.get().toBuilder()
            .sealedClaimUpload(YES)
            .build();

    public static final Supplier<ClaimSubmissionOperationIndicators> withClaimReceiptUploadOperationSuccess =
        () -> withSealedClaimUploadOperationSuccess.get().toBuilder()
            .claimIssueReceiptUpload(YES)
            .build();

    public static final Supplier<ClaimSubmissionOperationIndicators> withRpaOperationSuccess =
        () -> withClaimReceiptUploadOperationSuccess.get().toBuilder()
            .rpa(YES)
            .build();

    public static final Supplier<ClaimSubmissionOperationIndicators> withAllOperationSuccess =
        () -> withRpaOperationSuccess.get().toBuilder()
            .claimantNotification(YES)
            .build();

    private SampleClaimSubmissionOperationIndicators() {
        //Do nothing constructor.
    }

}
