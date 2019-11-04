package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimSubmissionOperationIndicators;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class PostClaimOrchestrationHandlerTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    private static final String SUBMITTER_NAME = "submitter-name";
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    private Map<String, Object> claimContents = new HashMap<>();
    private String claimTemplate = "claimTemplate";
    private Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private PostClaimOrchestrationHandler postClaimOrchestrationHandler;
    @Mock
    private CitizenServiceDocumentsService citizenServiceDocumentsService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private PDFServiceClient pdfServiceClient;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;
    @Mock
    private ClaimantOperationService claimantOperationService;
    @Mock
    private RpaOperationService rpaOperationService;
    @Mock
    private NotifyStaffOperationService notifyStaffOperationService;
    @Mock
    private UploadOperationService uploadOperationService;
    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;
    @Mock
    private PinOrchestrationService pinOrchestrationService;

    @Before
    public void before() {
        DocumentOrchestrationService documentOrchestrationService = new DocumentOrchestrationService(
            citizenServiceDocumentsService,
            sealedClaimPdfService,
            pdfServiceClient,
            claimIssueReceiptService,
            claimService,
            userService
        );

        postClaimOrchestrationHandler = new PostClaimOrchestrationHandler(
            documentOrchestrationService,
            pinOrchestrationService,
            uploadOperationService,
            claimantOperationService,
            rpaOperationService,
            notifyStaffOperationService,
            claimService
        );

        given(citizenServiceDocumentsService.sealedClaimDocument(any())).willReturn(sealedClaimLetterDocument);
        given(sealedClaimPdfService.createPdf(any())).willReturn(new PDF(
            "sealedClaim",
            PDF_BYTES,
            SEALED_CLAIM
        ));
        given(claimIssueReceiptService.createPdf(any())).willReturn(new PDF(
            "claimIssueReceipt",
            PDF_BYTES,
            CLAIM_ISSUE_RECEIPT
        ));
        given(pdfServiceClient.generateFromHtml(any(), anyMap())).willReturn(PDF_BYTES);

        given(pinOrchestrationService.process(eq(CLAIM), anyString(), anyString())).willReturn(CLAIM);
        given(claimantOperationService.notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION))).willReturn(CLAIM);
        given(rpaOperationService.notify(eq(CLAIM), eq(AUTHORISATION), any())).willReturn(CLAIM);
        given(notifyStaffOperationService.notify(eq(CLAIM), eq(AUTHORISATION), any())).willReturn(CLAIM);
        given(uploadOperationService.uploadDocument(eq(CLAIM), eq(AUTHORISATION), any())).willReturn(CLAIM);
    }

    @Test
    public void citizenIssueHandler() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(CLAIM));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
        verify(pinOrchestrationService).process(eq(CLAIM), anyString(), anyString());
        verify(claimantOperationService).notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2)).uploadDocument(eq(CLAIM),
            eq(AUTHORISATION), any());
        verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));

    }

    @Test
    public void citizenIssueHandlerWithClaimInCreate() {
        //given
        Claim claim = SampleClaim.getDefault().toBuilder().state(ClaimState.CREATE).build();
        given(claimantOperationService.notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION))).willReturn(claim);

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(CLAIM));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
        verify(pinOrchestrationService).process(eq(CLAIM), anyString(), anyString());
        verify(claimantOperationService).notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2)).uploadDocument(eq(CLAIM),
            eq(AUTHORISATION), any());
        verify(claimService).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));

    }

    @Test
    public void reSubmitWithAnyPinOperationFailsTriggersWholePinOperation() {
        //given
        Claim claimWithOnePinOperationFailure = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(
                SampleClaimSubmissionOperationIndicators.withOnePinOperationFailure.get()
            ).build();

        given(pinOrchestrationService.process(eq(claimWithOnePinOperationFailure), anyString(), anyString()))
            .willReturn(claimWithOnePinOperationFailure);
        given(claimantOperationService
            .notifyCitizen(eq(claimWithOnePinOperationFailure), any(), eq(AUTHORISATION)))
            .willReturn(claimWithOnePinOperationFailure);
        given(rpaOperationService
            .notify(eq(claimWithOnePinOperationFailure), eq(AUTHORISATION), any()))
            .willReturn(claimWithOnePinOperationFailure);
        given(uploadOperationService
            .uploadDocument(eq(claimWithOnePinOperationFailure), eq(AUTHORISATION), any()))
            .willReturn(claimWithOnePinOperationFailure);

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(claimWithOnePinOperationFailure,
            SUBMITTER_NAME, AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claimWithOnePinOperationFailure));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(claimWithOnePinOperationFailure));
        verify(pinOrchestrationService).process(eq(claimWithOnePinOperationFailure), anyString(), anyString());
        verify(claimantOperationService).notifyCitizen(eq(claimWithOnePinOperationFailure), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(claimWithOnePinOperationFailure), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2))
            .uploadDocument(eq(claimWithOnePinOperationFailure), eq(AUTHORISATION), any());

    }

    @Test
    public void reSubmitCitizenIssueHandlerWhenPinOperationPassed() {
        //given
        Claim claimWithPinOperationSucceededIndicator = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(
                SampleClaimSubmissionOperationIndicators.withPinOperationSuccess.get()
            ).build();

        given(claimantOperationService
            .notifyCitizen(eq(claimWithPinOperationSucceededIndicator), any(), eq(AUTHORISATION)))
            .willReturn(claimWithPinOperationSucceededIndicator);
        given(rpaOperationService
            .notify(eq(claimWithPinOperationSucceededIndicator), eq(AUTHORISATION), any()))
            .willReturn(claimWithPinOperationSucceededIndicator);
        given(uploadOperationService
            .uploadDocument(eq(claimWithPinOperationSucceededIndicator), eq(AUTHORISATION), any()))
            .willReturn(claimWithPinOperationSucceededIndicator);

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(
            claimWithPinOperationSucceededIndicator,
            SUBMITTER_NAME,
            AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verifyZeroInteractions(pinOrchestrationService);
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claimWithPinOperationSucceededIndicator));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(claimWithPinOperationSucceededIndicator));
        verify(claimantOperationService).notifyCitizen(eq(claimWithPinOperationSucceededIndicator), any(),
            eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(claimWithPinOperationSucceededIndicator), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2))
            .uploadDocument(eq(claimWithPinOperationSucceededIndicator),
                eq(AUTHORISATION), any());
    }

    @Test
    public void reSubmitCitizenIssueHandlerWhenAllOperationPassed() {
        //given
        Claim claimWithPinOperationSucceededIndicator = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(SampleClaimSubmissionOperationIndicators.withAllOperationSuccess.get())
            .build();

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(
            claimWithPinOperationSucceededIndicator,
            SUBMITTER_NAME,
            AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verifyZeroInteractions(pinOrchestrationService);
        verifyZeroInteractions(claimantOperationService);
        verifyZeroInteractions(rpaOperationService);
        verifyZeroInteractions(uploadOperationService);

    }

    @Test
    public void reSubmitCitizenIssueHandlerWhenUploadSealedClaimPassed() {
        //given
        Claim claimWithUploadSealedClaimSuccess = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(
                SampleClaimSubmissionOperationIndicators.withSealedClaimUploadOperationSuccess.get()
            ).build();

        given(claimantOperationService
            .notifyCitizen(eq(claimWithUploadSealedClaimSuccess), any(), eq(AUTHORISATION)))
            .willReturn(claimWithUploadSealedClaimSuccess);
        given(rpaOperationService
            .notify(eq(claimWithUploadSealedClaimSuccess), eq(AUTHORISATION), any()))
            .willReturn(claimWithUploadSealedClaimSuccess);
        given(uploadOperationService
            .uploadDocument(eq(claimWithUploadSealedClaimSuccess), eq(AUTHORISATION), any()))
            .willReturn(claimWithUploadSealedClaimSuccess);

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(
            claimWithUploadSealedClaimSuccess,
            SUBMITTER_NAME,
            AUTHORISATION);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claimWithUploadSealedClaimSuccess));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(claimWithUploadSealedClaimSuccess));
        verify(claimantOperationService).notifyCitizen(eq(claimWithUploadSealedClaimSuccess), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(claimWithUploadSealedClaimSuccess), eq(AUTHORISATION), any());
        verify(uploadOperationService).uploadDocument(eq(claimWithUploadSealedClaimSuccess),
            eq(AUTHORISATION), any());
        verifyZeroInteractions(pinOrchestrationService);

    }

    @Test
    public void reSubmitCitizenIssueHandlerWhenClaimReceiptUploadPassed() {
        //given
        Claim claimWithClaimReceiptUploadSuccess = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(
                SampleClaimSubmissionOperationIndicators.withClaimReceiptUploadOperationSuccess.get()
            ).build();

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(
            claimWithClaimReceiptUploadSuccess,
            SUBMITTER_NAME,
            AUTHORISATION);

        given(claimantOperationService
            .notifyCitizen(eq(claimWithClaimReceiptUploadSuccess), any(), eq(AUTHORISATION)))
            .willReturn(claimWithClaimReceiptUploadSuccess);
        given(rpaOperationService
            .notify(eq(claimWithClaimReceiptUploadSuccess), eq(AUTHORISATION), any()))
            .willReturn(claimWithClaimReceiptUploadSuccess);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claimWithClaimReceiptUploadSuccess));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(claimWithClaimReceiptUploadSuccess));
        verify(claimantOperationService).notifyCitizen(eq(claimWithClaimReceiptUploadSuccess),
            any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(claimWithClaimReceiptUploadSuccess), eq(AUTHORISATION), any());
        verifyZeroInteractions(uploadOperationService);
        verifyZeroInteractions(pinOrchestrationService);

    }

    @Test
    public void reSubmitCitizenIssueHandlerWhenRpaPassed() {
        //given
        Claim claimWithRpaSuccess = CLAIM.toBuilder()
            .claimSubmissionOperationIndicators(
                SampleClaimSubmissionOperationIndicators.withRpaOperationSuccess.get()
            ).build();

        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(
            claimWithRpaSuccess,
            SUBMITTER_NAME,
            AUTHORISATION);

        given(claimantOperationService
            .notifyCitizen(eq(claimWithRpaSuccess), any(), eq(AUTHORISATION)))
            .willReturn(claimWithRpaSuccess);

        //when
        postClaimOrchestrationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claimWithRpaSuccess));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(claimWithRpaSuccess));
        verify(claimantOperationService).notifyCitizen(eq(claimWithRpaSuccess), any(), eq(AUTHORISATION));
        verifyZeroInteractions(rpaOperationService);
        verifyZeroInteractions(uploadOperationService);
        verifyZeroInteractions(pinOrchestrationService);

    }

    @Test
    public void noOperationPerformedWhenPinOperationFails() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);
        doThrow(new RuntimeException("bulk print failed")).when(pinOrchestrationService).process(any(), any(), any());

        //when
        try {
            postClaimOrchestrationHandler.citizenIssueHandler(event);

        } finally {
            //then
            verify(uploadOperationService, never())
                .uploadDocument(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

            verify(rpaOperationService, never()).notify(any(Claim.class), eq(AUTHORISATION), any(PDF.class));
            verify(claimantOperationService, never())
                .notifyCitizen(any(Claim.class), eq(SUBMITTER_NAME), eq(AUTHORISATION));

            verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));
        }
    }

    @Test
    public void noOperationPerformedWhenRpaOperationFails() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);
        doThrow(new RuntimeException("notification failed"))
            .when(rpaOperationService).notify(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

        //when
        try {
            postClaimOrchestrationHandler.citizenIssueHandler(event);

        } finally {
            //then
            verify(uploadOperationService, atLeast(2))
                .uploadDocument(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

            verify(claimantOperationService, never())
                .notifyCitizen(any(Claim.class), eq(SUBMITTER_NAME), eq(AUTHORISATION));

            verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));
        }
    }

    @Test
    public void noOperationPerformedWhenUploadOperationFails() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        doThrow(new RuntimeException("notification failed"))
            .when(uploadOperationService)
            .uploadDocument(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

        //when
        try {
            postClaimOrchestrationHandler.citizenIssueHandler(event);

        } finally {
            //then

            verify(rpaOperationService, never()).notify(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

            verify(claimantOperationService, never())
                .notifyCitizen(any(Claim.class), eq(SUBMITTER_NAME), eq(AUTHORISATION));

            verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));
        }
    }

    @Test
    public void noOperationPerformedWhenClaimantNotifyOperationFails() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        doThrow(new RuntimeException("notification failed"))
            .when(claimantOperationService)
            .notifyCitizen(any(Claim.class), eq(SUBMITTER_NAME), eq(AUTHORISATION));

        //when
        try {
            postClaimOrchestrationHandler.citizenIssueHandler(event);

        } finally {
            //then

            verify(uploadOperationService, atLeast(2))
                .uploadDocument(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

            verify(rpaOperationService).notify(any(Claim.class), eq(AUTHORISATION), any(PDF.class));

            verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));
        }
    }

    @Test
    public void representativeIssueHandler() {
        //given
        RepresentedClaimCreatedEvent event = new RepresentedClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //when
        postClaimOrchestrationHandler.representativeIssueHandler(event);

        //then
        verify(sealedClaimPdfService).createPdf(eq(CLAIM));
        verify(claimantOperationService)
            .confirmRepresentative(eq(CLAIM), eq(SUBMITTER_NAME), anyString(), eq(AUTHORISATION));

        verify(rpaOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(notifyStaffOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService).uploadDocument(eq(CLAIM), eq(AUTHORISATION), any());
        verify(claimService, never()).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));

    }

    @Test
    public void representativeIssueHandlerWithClaimInCreate() {
        //given
        Claim claim = SampleClaim.getDefault().toBuilder().state(ClaimState.CREATE).build();
        given(claimantOperationService
            .confirmRepresentative(eq(CLAIM), eq(SUBMITTER_NAME), anyString(), eq(AUTHORISATION)))
            .willReturn(claim);

        RepresentedClaimCreatedEvent event = new RepresentedClaimCreatedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //when
        postClaimOrchestrationHandler.representativeIssueHandler(event);

        //then
        verify(sealedClaimPdfService).createPdf(eq(CLAIM));
        verify(claimantOperationService)
            .confirmRepresentative(eq(CLAIM), eq(SUBMITTER_NAME), anyString(), eq(AUTHORISATION));

        verify(rpaOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(notifyStaffOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService).uploadDocument(eq(CLAIM), eq(AUTHORISATION), any());
        verify(claimService).updateClaimState(eq(AUTHORISATION), any(Claim.class), eq(ClaimState.OPEN));
    }
}
