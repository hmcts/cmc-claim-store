package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimSubmissionOperationIndicatorsTest {

    @Test
    public void shouldReturnTrueWhenAllPinOperationsAreYes() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(YES)
            .staffNotification(YES)
            .defendantNotification(YES)
            .build();

        assertTrue(indicators.isPinOperationSuccess());
    }

    @Test
    public void shouldReturnFalseWhenBulkPrintInPinOperationsIsNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(NO)
            .staffNotification(YES)
            .defendantNotification(YES)
            .build();

        assertFalse(indicators.isPinOperationSuccess());
    }

    @Test
    public void shouldReturnFalseWhenStaffNotificationInPinOperationsIsNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(YES)
            .staffNotification(NO)
            .defendantNotification(YES)
            .build();

        assertFalse(indicators.isPinOperationSuccess());
    }

    @Test
    public void shouldReturnFalseWhenDefendantNotificationInPinOperationsIsNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(YES)
            .staffNotification(YES)
            .defendantNotification(NO)
            .build();

        assertFalse(indicators.isPinOperationSuccess());
    }

    @Test
    public void shouldReturnFalseWhenAllPinOperationsAreNo() {
        ClaimSubmissionOperationIndicators indicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(NO)
            .staffNotification(NO)
            .defendantNotification(NO)
            .build();

        assertFalse(indicators.isPinOperationSuccess());
    }
}
