package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDClaimSubmissionOperationIndicators {
    private CCDYesNoOption claimantNotification;
    private CCDYesNoOption defendantNotification;
    private CCDYesNoOption bulkPrint;
    private CCDYesNoOption rpa;
    private CCDYesNoOption staffNotification;
    private CCDYesNoOption sealedClaimUpload;
    private CCDYesNoOption claimIssueReceiptUpload;
}
