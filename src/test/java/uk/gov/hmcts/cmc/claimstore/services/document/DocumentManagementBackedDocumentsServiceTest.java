package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.ReviewOrderService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.documents.questionnaire.ClaimantDirectionsQuestionnairePdfService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementBackedDocumentsServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    private DocumentManagementBackedDocumentsService documentManagementBackedDocumentsService;

    @Mock
    private ClaimService claimService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;
    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;
    @Mock
    private SettlementAgreementCopyService settlementAgreementCopyService;
    @Mock
    private ReviewOrderService reviewOrderService;
    @Mock
    private ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;

    @Before
    public void setUp() {
        documentManagementBackedDocumentsService = new DocumentManagementBackedDocumentsService(
            claimService,
            documentManagementService,
            sealedClaimPdfService,
            claimIssueReceiptService,
            defendantResponseReceiptService,
            settlementAgreementCopyService,
            reviewOrderService,
            claimantDirectionsQuestionnairePdfService
        );
    }

    @Test
    public void shouldGenerateSealedClaim() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(sealedClaimPdfService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "sealedClaim",
            PDF_BYTES,
            SEALED_CLAIM
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateClaimIssueReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(claimIssueReceiptService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "claimIssueReceipt",
            PDF_BYTES,
            CLAIM_ISSUE_RECEIPT
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            CLAIM_ISSUE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateDefendantResponseReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(defendantResponseReceiptService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "defendantResponseReceipt",
            PDF_BYTES,
            DEFENDANT_RESPONSE_RECEIPT
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            DEFENDANT_RESPONSE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateSettlementAgreement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(settlementAgreementCopyService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "settlementAgreementCopy",
            PDF_BYTES,
            SETTLEMENT_AGREEMENT
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SETTLEMENT_AGREEMENT,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateReviewOrderRequest() {
        Claim claim = SampleClaim.builder()
            .withReviewOrder(SampleReviewOrder.getDefault()
            ).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(reviewOrderService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "reviewOrder",
            PDF_BYTES,
            REVIEW_ORDER
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            REVIEW_ORDER,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldNotUploadDocumentIfItAlreadyExists() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);

        when(documentManagementService.downloadDocument(eq(AUTHORISATION), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);

        documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
        verify(documentManagementService, atLeastOnce()).downloadDocument(
            eq(AUTHORISATION),
            any(ClaimDocument.class));
        verify(documentManagementService, never()).uploadDocument(anyString(), any(PDF.class));
        verify(claimService, never()).saveClaimDocuments(
            eq(AUTHORISATION),
            eq(claim.getId()),
            any(ClaimDocumentCollection.class),
            any(ClaimDocumentType.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfDocumentTypeDoesNotHaveAService() {
        documentManagementBackedDocumentsService.generateDocument(
            "1234",
            CCJ_REQUEST,
            AUTHORISATION);
    }

    @Test
    public void shouldGenerateClaimantHearingRequirement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(claimantDirectionsQuestionnairePdfService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "claimantDirectionsQuestionnaire",
            PDF_BYTES,
            CLAIMANT_DIRECTIONS_QUESTIONNAIRE
        ));
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            CLAIMANT_DIRECTIONS_QUESTIONNAIRE,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGetOrderDocumentsForMediationAgreement() {
        final ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        final ClaimDocumentType documentType = MEDIATION_AGREEMENT;
        final ClaimDocument claimDocument = new ClaimDocument("", null, null, "", documentType, null, null,
            null, "", 1L);
        claimDocumentCollection.addClaimDocument(claimDocument);
        Claim claim = Claim.builder()
            .externalId("externalID")
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            documentType,
            AUTHORISATION
        );
        verify(documentManagementService, once()).downloadDocument(any(), any());
    }

    @Test
    public void shouldGetOrderDocumentsForOrderSanctions() {
        final ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        final ClaimDocumentType documentType = ORDER_SANCTIONS;
        final ClaimDocument claimDocument = new ClaimDocument("", null, null, "", documentType, null, null,
            null, "", 1L);
        claimDocumentCollection.addClaimDocument(claimDocument);
        Claim claim = Claim.builder()
            .externalId("externalID")
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            documentType,
            AUTHORISATION
        );
        verify(documentManagementService, once()).downloadDocument(any(), any());
    }

    @Test
    public void shouldGetOrderDocumentsForOrderDirections() {
        final ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        final ClaimDocumentType documentType = ORDER_DIRECTIONS;
        final ClaimDocument claimDocument = new ClaimDocument("", null, null, "", documentType, null, null,
            null, "", 1L);
        claimDocumentCollection.addClaimDocument(claimDocument);
        Claim claim = Claim.builder()
            .externalId("externalID")
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            documentType,
            AUTHORISATION
        );
        verify(documentManagementService, once()).downloadDocument(any(), any());
    }

    private void verifyCommon(byte[] pdf) {
        assertArrayEquals(PDF_BYTES, pdf);
        verify(documentManagementService).uploadDocument(anyString(), any(PDF.class));
    }
}
