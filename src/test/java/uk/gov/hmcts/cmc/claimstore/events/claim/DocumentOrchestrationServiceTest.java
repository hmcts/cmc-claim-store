package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentOrchestrationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final String PIN = "PIN";
    public static final String LETTER_HOLDER_ID = "LetterHolderId";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final String DOC_URL = "http://success.test";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();
    private static final GeneratePinResponse PIN_RESPONSE = new GeneratePinResponse("PIN", "userid");
    private static Map<String, Object> VALUES = Collections.emptyMap();
    private static final Document COVER_DOCUMENT = new Document(DOC_URL, VALUES);
    private final Map<String, Object> claimContents = new HashMap<>();
    private final String claimTemplate = "claimTemplate";
    private final Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private final Map<String, Object> pinContents = new HashMap<>();
    private final String pinTemplate = "pinTemplate";
    private final Document defendantLetterDocument = new Document(pinTemplate, pinContents);

    @Mock
    private CitizenServiceDocumentsService citizenServiceDocumentsService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private PDFServiceClient pdfServiceClient;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;
    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;
    private DocumentOrchestrationService documentOrchestrationService;
    @Mock
    private PrintableDocumentService printableDocumentService;

    @Before
    public void before() {
        documentOrchestrationService = new DocumentOrchestrationService(
            citizenServiceDocumentsService,
            sealedClaimPdfService,
            pdfServiceClient,
            claimIssueReceiptService,
            claimService,
            userService,
            printableDocumentService
        );

        given(citizenServiceDocumentsService.sealedClaimDocument(eq(CLAIM))).willReturn(sealedClaimLetterDocument);
        given(sealedClaimPdfService.createPdf(eq(CLAIM))).willReturn(new PDF(
            "sealedClaim",
            PDF_BYTES,
            ClaimDocumentType.SEALED_CLAIM
        ));
        given(claimIssueReceiptService.createPdf(eq(CLAIM))).willReturn(new PDF(
            "claimIssueReceipt",
            PDF_BYTES,
            CLAIM_ISSUE_RECEIPT
        ));
        given(pdfServiceClient.generateFromHtml(any(), anyMap())).willReturn(PDF_BYTES);
    }

    @Test
    public void shouldCreateAllDocumentsForCitizen() {
        given(printableDocumentService.pdf(DOCUMENT, AUTHORISATION)).willReturn(PDF_BYTES);
        given(userService.generatePin(anyString(), anyString())).willReturn(PIN_RESPONSE);
        given(citizenServiceDocumentsService
            .createDefendantPinLetter(CLAIM, PIN, AUTHORISATION))
            .willReturn(DOCUMENT);
        given(printableDocumentService.process(DOCUMENT, AUTHORISATION)).willReturn(COVER_DOCUMENT);
        // when
        documentOrchestrationService.generateForCitizen(CLAIM, AUTHORISATION, true);

        //verify
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(CLAIM));
        verify(citizenServiceDocumentsService).createDefendantPinLetter(eq(CLAIM), eq(PIN), eq(AUTHORISATION));
        verify(printableDocumentService).process(eq(DOCUMENT), eq(AUTHORISATION));
        verify(pdfServiceClient, atLeast(1)).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
        verify(printableDocumentService).pdf(DOCUMENT, AUTHORISATION);
    }

    @Test
    public void shouldReturnSealedClaimPdf() {
        // when
        documentOrchestrationService.getSealedClaimPdf(CLAIM);

        //verify
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(CLAIM));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
    }

    @Test
    public void shouldReturnClaimIssueReceiptPdf() {
        // when
        documentOrchestrationService.getClaimIssueReceiptPdf(CLAIM);

        //verify
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
    }

    @Test
    public void shouldReturnSealedClaimForRepresentative() {
        // when
        documentOrchestrationService.getSealedClaimForRepresentative(CLAIM);

        //verify
        verify(sealedClaimPdfService).createPdf(eq(CLAIM));
    }
}
