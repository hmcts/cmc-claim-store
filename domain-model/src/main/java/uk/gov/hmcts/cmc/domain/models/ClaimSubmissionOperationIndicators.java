package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Builder
@Value
public class ClaimSubmissionOperationIndicators {
    private YesNoOption claimantNotification;
    private YesNoOption defendantNotification;
    private YesNoOption bulkPrint;
    private YesNoOption roboticProcessAutomation;
    private YesNoOption staffNotification;
    private YesNoOption sealedClaimUpload;
    private YesNoOption claimIssueReceiptUpload;
    private YesNoOption defendantPinLetterUpload;
}
