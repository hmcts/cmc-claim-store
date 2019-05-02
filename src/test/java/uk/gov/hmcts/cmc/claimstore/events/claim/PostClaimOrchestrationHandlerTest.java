package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
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
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostClaimOrchestrationHandlerTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String PIN = "PIN";
    public static final String SUBMITTER_NAME = "submitter-name";
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
    public static final String LETTER_HOLDER_ID = "LetterHolderId";

    private Map<String, Object> pinContents = new HashMap<>();
    private String pinTemplate = "pinTemplate";
    private Document defendantLetterDocument = new Document(pinTemplate, pinContents);

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
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

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
            eventsStatusService,
            claimService
        );

        given(userService.generatePin(eq(CLAIM.getClaimData().getDefendant().getName()), eq(AUTHORISATION)))
            .willReturn(GeneratePinResponse.builder()
                .pin(PIN)
                .userId(LETTER_HOLDER_ID)
                .build()
            );

        given(citizenServiceDocumentsService.pinLetterDocument(eq(CLAIM), eq(PIN))).willReturn(defendantLetterDocument);
        given(citizenServiceDocumentsService.sealedClaimDocument(eq(CLAIM))).willReturn(sealedClaimLetterDocument);
        given(sealedClaimPdfService.createPdf(eq(CLAIM))).willReturn(PDF_BYTES);
        given(pdfServiceClient.generateFromHtml(any(), anyMap())).willReturn(PDF_BYTES);
        given(claimIssueReceiptService.createPdf(eq(CLAIM))).willReturn(PDF_BYTES);
        given(pinOrchestrationService.process(eq(CLAIM), anyString(), anyString(), any())).willReturn(CLAIM);
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
        verify(pdfServiceClient, atLeast(2)).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
        verify(pinOrchestrationService).process(eq(CLAIM), anyString(), anyString(), any());
        verify(claimantOperationService).notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(CLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2)).uploadDocument(eq(CLAIM),
            eq(AUTHORISATION), any());

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
    }
}
