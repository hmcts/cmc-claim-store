package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import java.util.function.Function;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.valueOf;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.fromValue;

public interface ClaimSubmissionOperationIndicatorMapper {
    Function<CCDClaimSubmissionOperationIndicators, ClaimSubmissionOperationIndicators>
        mapFromCCDClaimSubmissionOperationIndicators = claimSubmissionOperationIndicators ->
        ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(fromValue(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(fromValue(claimSubmissionOperationIndicators.getDefendantNotification().name()))
            .bulkPrint(fromValue(claimSubmissionOperationIndicators.getBulkPrint().name()))
            .rpa(fromValue(claimSubmissionOperationIndicators.getRpa().name()))
            .staffNotification(fromValue(claimSubmissionOperationIndicators.getStaffNotification().name()))
            .sealedClaimUpload(fromValue(claimSubmissionOperationIndicators.getSealedClaimUpload().name()))
            .claimIssueReceiptUpload(fromValue(claimSubmissionOperationIndicators.getClaimIssueReceiptUpload().name()))
            .build();

    Function<ClaimSubmissionOperationIndicators, CCDClaimSubmissionOperationIndicators>
        mapClaimSubmissionOperationIndicatorsToCCD = claimSubmissionOperationIndicators ->
        CCDClaimSubmissionOperationIndicators.builder()
            .claimantNotification(valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(valueOf(claimSubmissionOperationIndicators.getDefendantNotification().name()))
            .bulkPrint(valueOf(claimSubmissionOperationIndicators.getBulkPrint().name()))
            .rpa(valueOf(claimSubmissionOperationIndicators.getRpa().name()))
            .staffNotification(valueOf(claimSubmissionOperationIndicators.getStaffNotification().name()))
            .sealedClaimUpload(valueOf(claimSubmissionOperationIndicators.getSealedClaimUpload().name()))
            .claimIssueReceiptUpload(valueOf(claimSubmissionOperationIndicators.getClaimIssueReceiptUpload().name()))
            .build();
}
