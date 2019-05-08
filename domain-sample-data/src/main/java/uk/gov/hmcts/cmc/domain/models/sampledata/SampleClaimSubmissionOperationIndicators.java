package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Supplier;

public class SampleClaimSubmissionOperationIndicators {

    public static Supplier<ClaimSubmissionOperationIndicators> withPinOperationSuccess =
        () -> getClaimSubmissionIndicatorBuilderByAction("PIN").build();

    public static Supplier<ClaimSubmissionOperationIndicators> withOnePinOperationFailure =
        () -> getClaimSubmissionIndicatorBuilderByAction("PIN")
            .bulkPrint(YesNoOption.NO)
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> withAllOperationSuccess =
        () -> getClaimSubmissionIndicatorBuilderByAction("CLAIMANT_NOTIFY").build();

    public static Supplier<ClaimSubmissionOperationIndicators> withSealedClaimUploadOperationSuccess =
        () -> getClaimSubmissionIndicatorBuilderByAction("SEALED_CLAIM_UPLOAD").build();

    public static Supplier<ClaimSubmissionOperationIndicators> withClaimReceiptUploadOperationSuccess =
        () -> getClaimSubmissionIndicatorBuilderByAction("CLAIM_RECEIPT_UPLOAD").build();

    public static Supplier<ClaimSubmissionOperationIndicators> withRpaOperationSuccess =
        () -> getClaimSubmissionIndicatorBuilderByAction("RPA").build();

    private SampleClaimSubmissionOperationIndicators() {
        //Do nothing constructor.
    }

    @SuppressWarnings("checkstyle:FallThrough")
    private static ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder
        getClaimSubmissionIndicatorBuilderByAction(String action) {

        ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder returnBuilder =
            ClaimSubmissionOperationIndicators.builder();

        switch (action) {
            case "CLAIMANT_NOTIFY":
                returnBuilder.claimantNotification(YesNoOption.YES);
            case "RPA":
                returnBuilder.rpa(YesNoOption.YES);
            case "CLAIM_RECEIPT_UPLOAD":
                returnBuilder.claimIssueReceiptUpload(YesNoOption.YES);
            case "SEALED_CLAIM_UPLOAD":
                returnBuilder.sealedClaimUpload(YesNoOption.YES);
            case "PIN":
                returnBuilder.defendantPinLetterUpload(YesNoOption.YES)
                    .bulkPrint(YesNoOption.YES)
                    .staffNotification(YesNoOption.YES)
                    .defendantNotification(YesNoOption.YES);
                break;
            default:
                returnBuilder.bulkPrint(YesNoOption.NO);

        }

        return returnBuilder;
    }

}
