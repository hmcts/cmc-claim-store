package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimSubmissionOperationIndicatorsTest {

    @Test
    public void shouldReturnTrueWhenAllOperationsAreYes() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(YES)
            .defendantNotification(YES)
            .bulkPrint(YES)
            .rpa(YES)
            .staffNotification(YES)
            .sealedClaimUpload(YES)
            .claimIssueReceiptUpload(YES)
            .build();

        assertTrue(indicators.isAllSuccess());
    }

    @Test
    public void shouldReturnFalseWhenAllOperationsAreNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(NO)
            .defendantNotification(NO)
            .bulkPrint(NO)
            .rpa(NO)
            .staffNotification(NO)
            .sealedClaimUpload(NO)
            .claimIssueReceiptUpload(NO)
            .build();

        assertFalse(indicators.isAllSuccess());
    }

    @Test
    public void shouldReturnFalseWhenAnyOperationsIsNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(NO)
            .defendantNotification(YES)
            .bulkPrint(YES)
            .rpa(YES)
            .staffNotification(YES)
            .sealedClaimUpload(YES)
            .claimIssueReceiptUpload(YES)
            .build();

        assertFalse(indicators.isAllSuccess());
    }
}
