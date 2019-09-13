package uk.gov.hmcts.cmc.ccd;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@FunctionalInterface
public interface SampleCCDClaimSubmissionOperationIndicators<S, T, U, V, W, X, Y, R> {

    SampleCCDClaimSubmissionOperationIndicators<CCDYesNoOption, CCDYesNoOption, CCDYesNoOption, CCDYesNoOption,
        CCDYesNoOption, CCDYesNoOption, CCDYesNoOption, CCDClaimSubmissionOperationIndicators>
        getClaimSubmissionIndicatorWithVaue = (S, T, U, V, W, X, Y) ->
        CCDClaimSubmissionOperationIndicators.builder()
            .claimantNotification(S)
            .defendantNotification(T)
            .bulkPrint(U)
            .rpa(V)
            .staffNotification(W)
            .sealedClaimUpload(X)
            .claimIssueReceiptUpload(Y)
            .build();

    CCDClaimSubmissionOperationIndicators defaultCCDClaimSubmissionOperationIndicators =
        getClaimSubmissionIndicatorWithVaue.get(CCDYesNoOption.NO, CCDYesNoOption.NO,
            CCDYesNoOption.NO, CCDYesNoOption.NO, CCDYesNoOption.NO, CCDYesNoOption.NO,
            CCDYesNoOption.NO);

    CCDClaimSubmissionOperationIndicators CCDClaimSubmissionOperationIndicatorsWithPinSuccess =
        getClaimSubmissionIndicatorWithVaue.get(CCDYesNoOption.NO, CCDYesNoOption.YES,
            CCDYesNoOption.YES, CCDYesNoOption.NO, CCDYesNoOption.YES, CCDYesNoOption.NO, CCDYesNoOption.NO);

    R get(S claimantNotification, T defendantNotification, U bulkPrint, V rpa,
          W staffNotification, X sealedClaimUpload, Y claimIssueReceiptUpload);

}
