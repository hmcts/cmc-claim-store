package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class ClaimCreationEventsStatusServiceTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final Claim CLAIM = SampleClaim.getDefault();

    @Mock
    private CaseRepository caseRepository;

    private ClaimCreationEventsStatusService eventsStatusService;

    @Before
    public void setUp() {
        eventsStatusService = new ClaimCreationEventsStatusService(caseRepository);
    }

    @Test
    public void updateClaimOperationByPinOperationCaseEvent() {
        ClaimSubmissionOperationIndicators operationIndicatorWithPinCompletion =
            ClaimSubmissionOperationIndicators.builder().defendantNotification(YES)
                .bulkPrint(YES)
                .defendantPinLetterUpload(YES)
                .staffNotification(YES).build();

        when(caseRepository.updateClaimSubmissionOperationStatus(any(), any(), any(), any())).thenReturn(CLAIM);

        eventsStatusService.updateClaimOperationCompletion(AUTHORISATION, CLAIM,
            CaseEvent.PIN_GENERATION_OPERATIONS);

        verify(caseRepository).updateClaimSubmissionOperationStatus(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicatorWithPinCompletion), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test
    public void updateClaimOperationByClaimIssueReceiptUploadEvent() {
        ClaimSubmissionOperationIndicators operationIndicatorWithPinCompletion =
            ClaimSubmissionOperationIndicators.builder()
                .claimIssueReceiptUpload(YES)
                .build();

        when(caseRepository.updateClaimSubmissionOperationStatus(any(), any(), any(), any())).thenReturn(CLAIM);

        eventsStatusService.updateClaimOperationCompletion(AUTHORISATION, CLAIM,
            CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD);

        verify(caseRepository).updateClaimSubmissionOperationStatus(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicatorWithPinCompletion), eq(CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD));
    }

    @Test
    public void updateClaimOperationByLinkSealedClaimEvent() {
        ClaimSubmissionOperationIndicators operationIndicatorWithPinCompletion =
            ClaimSubmissionOperationIndicators.builder()
                .sealedClaimUpload(YES)
                .build();

        when(caseRepository.updateClaimSubmissionOperationStatus(any(), any(), any(), any())).thenReturn(CLAIM);

        eventsStatusService.updateClaimOperationCompletion(AUTHORISATION, CLAIM,
            CaseEvent.SEALED_CLAIM_UPLOAD);

        verify(caseRepository).updateClaimSubmissionOperationStatus(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicatorWithPinCompletion), eq(CaseEvent.SEALED_CLAIM_UPLOAD));
    }

    @Test
    public void updateClaimOperationBySendRpaEvent() {
        ClaimSubmissionOperationIndicators operationIndicatorWithPinCompletion =
            ClaimSubmissionOperationIndicators.builder()
                .rpa(YES)
                .build();

        when(caseRepository.updateClaimSubmissionOperationStatus(any(), any(), any(), any())).thenReturn(CLAIM);

        eventsStatusService.updateClaimOperationCompletion(AUTHORISATION, CLAIM,
            CaseEvent.SENDING_RPA);

        verify(caseRepository).updateClaimSubmissionOperationStatus(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicatorWithPinCompletion), eq(CaseEvent.SENDING_RPA));
    }

    @Test
    public void updateClaimOperationBySendClaimantNotificationEvent() {
        ClaimSubmissionOperationIndicators operationIndicatorWithPinCompletion =
            ClaimSubmissionOperationIndicators.builder()
                .claimantNotification(YES)
                .build();

        when(caseRepository.updateClaimSubmissionOperationStatus(any(), any(), any(), any())).thenReturn(CLAIM);

        eventsStatusService.updateClaimOperationCompletion(AUTHORISATION, CLAIM,
            CaseEvent.SENDING_CLAIMANT_NOTIFICATION);

        verify(caseRepository).updateClaimSubmissionOperationStatus(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicatorWithPinCompletion), eq(CaseEvent.SENDING_CLAIMANT_NOTIFICATION));
    }
}
