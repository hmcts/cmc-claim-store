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
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentOrchestrationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
    public static final String PIN = "PIN";
    public static final String LETTER_HOLDER_ID = "LetterHolderId";

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

    @Before
    public void before() {
        documentOrchestrationService = new DocumentOrchestrationService(
            citizenServiceDocumentsService,
            sealedClaimPdfService,
            pdfServiceClient,
            claimIssueReceiptService,
            claimService,
            userService
        );

        given(citizenServiceDocumentsService.sealedClaimDocument(eq(CLAIM))).willReturn(sealedClaimLetterDocument);
        given(citizenServiceDocumentsService.pinLetterDocument(eq(CLAIM), eq(PIN))).willReturn(defendantLetterDocument);
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

        given(userService.generatePin(eq(CLAIM.getClaimData().getDefendant().getName()), eq(AUTHORISATION)))
            .willReturn(GeneratePinResponse.builder()
                .pin(PIN)
                .userId(LETTER_HOLDER_ID)
                .build()
            );
    }

    @Test
    public void shouldCreateAllDocumentsForCitizen() {
        // when
        documentOrchestrationService.generateForCitizen(CLAIM, AUTHORISATION);

        //verify
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(CLAIM));
        verify(citizenServiceDocumentsService).pinLetterDocument(eq(CLAIM), eq(PIN));
        verify(pdfServiceClient, atLeast(2)).generateFromHtml(any(), anyMap());
        verify(claimIssueReceiptService).createPdf(eq(CLAIM));
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
