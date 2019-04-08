package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ClaimSubmissionOperationIndicators {
    private boolean claimantNotification;
    private boolean defendantNotification;
    private boolean bulkPrint;
    private boolean RPA;
    private boolean staffNotification;
    private boolean sealedClaimUpload;
    private boolean claimIssueReceiptUpload;
    private boolean defendantPinLetterUpload;
}
