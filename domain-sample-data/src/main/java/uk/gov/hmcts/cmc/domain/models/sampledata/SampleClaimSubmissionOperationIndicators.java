package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import java.util.function.Supplier;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class SampleClaimSubmissionOperationIndicators {

    public static Supplier<ClaimSubmissionOperationIndicators> withAllOperationDefaulted =
        () -> ClaimSubmissionOperationIndicators.builder()
            .defendantNotification(NO)
            .rpa(NO)
            .claimIssueReceiptUpload(NO)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withPinOperationSuccess =
        () -> withAllOperationDefaulted.get().toBuilder()
            .bulkPrint(YES)
            .staffNotification(YES)
            .defendantNotification(YES)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withOnePinOperationFailure =
        () -> withPinOperationSuccess.get().toBuilder()
            .bulkPrint(NO)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withSealedClaimUploadOperationSuccess =
        () -> withPinOperationSuccess.get().toBuilder()
            .sealedClaimUpload(YES)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withClaimReceiptUploadOperationSuccess =
        () -> withSealedClaimUploadOperationSuccess.get().toBuilder()
            .claimIssueReceiptUpload(YES)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withRpaOperationSuccess =
        () -> withClaimReceiptUploadOperationSuccess.get().toBuilder()
            .rpa(YES)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withAllOperationSuccess =
        () -> withRpaOperationSuccess.get().toBuilder()
            .claimantNotification(YES)
            .build();

    private SampleClaimSubmissionOperationIndicators() {
        //Do nothing constructor.
    }

}
