package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHwfClaim;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;

@RunWith(MockitoJUnitRunner.class)
public class PostHwfClaimOrchestrationHandlerTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final Claim UPDATEDCLAIM = SampleClaim.getDefaultWithClaimStateAsCreate();
    public static final Claim CLAIM_HWF = SampleHwfClaim.getDefaultHwfPending();
    private static final String SUBMITTER_NAME = "submitter-name";
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    private final Map<String, Object> claimContents = new HashMap<>();
    private final String claimTemplate = "claimTemplate";
    private final Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private PostHwfClaimOrchestrationHandler postHwfClaimOrchestrationHandler;
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
    private UploadOperationService uploadOperationService;
    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;
    @Mock
    private PinOrchestrationService pinOrchestrationService;
    @Mock
    private AppInsights appInsights;
    @Mock
    private PrintableDocumentService printableDocumentService;

    @Before
    public void before() {
        DocumentOrchestrationService documentOrchestrationService = new DocumentOrchestrationService(
            citizenServiceDocumentsService,
            sealedClaimPdfService,
            pdfServiceClient,
            claimIssueReceiptService,
            claimService,
            userService,
            printableDocumentService
        );

        postHwfClaimOrchestrationHandler = new PostHwfClaimOrchestrationHandler(
            documentOrchestrationService,
            pinOrchestrationService,
            uploadOperationService,
            claimantOperationService,
            rpaOperationService,
            claimService,
            appInsights
        );

        given(citizenServiceDocumentsService.sealedClaimDocument(any())).willReturn(sealedClaimLetterDocument);

        given(claimIssueReceiptService.createPdf(any())).willReturn(new PDF(
            "claimIssueReceipt",
            PDF_BYTES,
            CLAIM_ISSUE_RECEIPT
        ));
        given(pdfServiceClient.generateFromHtml(any(), anyMap())).willReturn(PDF_BYTES);
    }

    @Test
    public void caseWorkerHwfClaimIssueHandler() {
        given(pinOrchestrationService.process(eq(CLAIM), anyString(), anyString())).willReturn(CLAIM);
        given(claimantOperationService.notifyCitizen(eq(CLAIM), any(), eq(AUTHORISATION))).willReturn(CLAIM);
        given(rpaOperationService.notify(eq(CLAIM), eq(AUTHORISATION), any())).willReturn(CLAIM);
        given(uploadOperationService.uploadDocument(eq(CLAIM), eq(AUTHORISATION), any())).willReturn(CLAIM);
        //given
        CaseworkerHwfClaimIssueEvent event = new CaseworkerHwfClaimIssueEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //when
        postHwfClaimOrchestrationHandler.caseworkerHwfClaimIssueEvent(event);

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
        verifyNoInteractions(appInsights);
    }

    @Test
    public void caseWorkerHwfClaimIssueHandlerWhenClaimIsInCreateState() {
        given(pinOrchestrationService.process(eq(UPDATEDCLAIM), anyString(), anyString())).willReturn(UPDATEDCLAIM);
        given(claimantOperationService.notifyCitizen(eq(UPDATEDCLAIM), any(),
            eq(AUTHORISATION))).willReturn(UPDATEDCLAIM);
        given(rpaOperationService.notify(eq(UPDATEDCLAIM), eq(AUTHORISATION), any()))
            .willReturn(UPDATEDCLAIM);
        given(uploadOperationService.uploadDocument(eq(UPDATEDCLAIM), eq(AUTHORISATION),
            any())).willReturn(UPDATEDCLAIM);

        //given
        CaseworkerHwfClaimIssueEvent event = new CaseworkerHwfClaimIssueEvent(UPDATEDCLAIM,
            SUBMITTER_NAME, AUTHORISATION);

        //when
        postHwfClaimOrchestrationHandler.caseworkerHwfClaimIssueEvent(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(UPDATEDCLAIM));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(UPDATEDCLAIM));
        verify(pinOrchestrationService).process(eq(UPDATEDCLAIM), anyString(), anyString());
        verify(claimantOperationService).notifyCitizen(eq(UPDATEDCLAIM), any(), eq(AUTHORISATION));
        verify(rpaOperationService).notify(eq(UPDATEDCLAIM), eq(AUTHORISATION), any());
        verify(uploadOperationService, atLeast(2)).uploadDocument(eq(UPDATEDCLAIM),
            eq(AUTHORISATION), any());
    }

    @Test
    public void caseWorkerHwfClaimIssueHandlerWhenClaimIsInCreateStateThrowingException() {
        given(pinOrchestrationService.process(eq(UPDATEDCLAIM), anyString(), anyString()))
            .willReturn(UPDATEDCLAIM);

        given(uploadOperationService.uploadDocument(eq(UPDATEDCLAIM), eq(AUTHORISATION), any()))
            .willThrow(new RuntimeException("Test"));

        //given
        CaseworkerHwfClaimIssueEvent event = new CaseworkerHwfClaimIssueEvent(UPDATEDCLAIM,
            SUBMITTER_NAME, AUTHORISATION);

        //when
        postHwfClaimOrchestrationHandler.caseworkerHwfClaimIssueEvent(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(UPDATEDCLAIM));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(UPDATEDCLAIM));
        verify(pinOrchestrationService).process(eq(UPDATEDCLAIM), anyString(), anyString());
    }
}
