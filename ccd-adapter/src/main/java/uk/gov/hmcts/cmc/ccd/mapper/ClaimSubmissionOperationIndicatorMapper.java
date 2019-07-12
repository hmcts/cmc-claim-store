package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

@Component
public class ClaimSubmissionOperationIndicatorMapper {

    private final YesNoMapper yesNoMapper;

    public ClaimSubmissionOperationIndicatorMapper(YesNoMapper yesNoMapper) {
        this.yesNoMapper = yesNoMapper;
    }

    public ClaimSubmissionOperationIndicators from(CCDClaimSubmissionOperationIndicators ccdOperationIndicators) {
        return ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(yesNoMapper.from(ccdOperationIndicators.getClaimantNotification()))
            .defendantNotification(yesNoMapper.from(ccdOperationIndicators.getDefendantNotification()))
            .bulkPrint(yesNoMapper.from(ccdOperationIndicators.getBulkPrint()))
            .rpa(yesNoMapper.from(ccdOperationIndicators.getRpa()))
            .staffNotification(yesNoMapper.from(ccdOperationIndicators.getStaffNotification()))
            .sealedClaimUpload(yesNoMapper.from(ccdOperationIndicators.getSealedClaimUpload()))
            .claimIssueReceiptUpload(yesNoMapper.from(ccdOperationIndicators.getClaimIssueReceiptUpload()))
            .build();
    }

    public CCDClaimSubmissionOperationIndicators to(ClaimSubmissionOperationIndicators operationIndicators) {

        return CCDClaimSubmissionOperationIndicators.builder()
            .claimantNotification(yesNoMapper.to(operationIndicators.getClaimantNotification()))
            .defendantNotification(yesNoMapper.to(operationIndicators.getDefendantNotification()))
            .bulkPrint(yesNoMapper.to(operationIndicators.getBulkPrint()))
            .rpa(yesNoMapper.to(operationIndicators.getRpa()))
            .staffNotification(yesNoMapper.to(operationIndicators.getStaffNotification()))
            .sealedClaimUpload(yesNoMapper.to(operationIndicators.getSealedClaimUpload()))
            .claimIssueReceiptUpload(yesNoMapper.to(operationIndicators.getClaimIssueReceiptUpload()))
            .build();
    }
}
