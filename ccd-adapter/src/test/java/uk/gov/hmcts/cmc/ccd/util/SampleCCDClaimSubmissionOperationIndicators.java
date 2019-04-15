package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.util.function.Supplier;

@FunctionalInterface
public interface SampleCCDClaimSubmissionOperationIndicators<S,T, U, V, W, X, Y, Z, R> {
     R get(S claimantNotification, T defendantNotification, U bulkPrint, V RPA,
           W staffNotification, X sealedClaimUpload, Y claimIssueReceiptUpload, Z defendantPinLetterUpload);

    SampleCCDClaimSubmissionOperationIndicators<CCDYesNoOption, CCDYesNoOption, CCDYesNoOption, CCDYesNoOption,
        CCDYesNoOption, CCDYesNoOption, CCDYesNoOption, CCDYesNoOption, CCDClaimSubmissionOperationIndicators>
        getClaimSubmissionIndicatorWithVaue = (S, T, U, V, W, X, Y, Z) ->
        CCDClaimSubmissionOperationIndicators.builder()
            .defendantPinLetterUpload(S)
            .bulkPrint(T)
            .staffNotification(U)
            .defendantNotification(V)
            .claimantNotification(W)
            .RPA(X)
            .sealedClaimUpload(Y)
            .claimIssueReceiptUpload(Z)
            .build();

     Supplier<CCDClaimSubmissionOperationIndicators> getDefaultCCDClaimSubmissionOperationIndicators =
         () -> getClaimSubmissionIndicatorWithVaue.get(CCDYesNoOption.NO, CCDYesNoOption.NO,
             CCDYesNoOption.NO, CCDYesNoOption.NO, CCDYesNoOption.NO, CCDYesNoOption.NO,
             CCDYesNoOption.NO, CCDYesNoOption.NO);

    Supplier<CCDClaimSubmissionOperationIndicators> getCCDClaimSubmissionOperationIndicatorsWithPinSuccess =
        () -> getClaimSubmissionIndicatorWithVaue.get(CCDYesNoOption.YES, CCDYesNoOption.YES,
            CCDYesNoOption.YES, CCDYesNoOption.YES, CCDYesNoOption.NO, CCDYesNoOption.NO,
            CCDYesNoOption.NO, CCDYesNoOption.NO);

    Supplier<CCDClaimSubmissionOperationIndicators>     getNullCCDClaimSubmissionOperationIndicators =
        () -> null;
}
