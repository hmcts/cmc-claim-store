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
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;
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
import static uk.gov.hmcts.cmc.domain.models.ClaimDocument.builder;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype.OCON9X;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.FORM;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementBackedDocumentsServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final User DEFENDANT = SampleUser.getDefaultDefendant();
    private static final User CLAIMANT = SampleUser.getDefaultClaimant();

    private DocumentManagementBackedDocumentsService documentManagementBackendDocumentsService;

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
    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        documentManagementBackendDocumentsService = new DocumentManagementBackedDocumentsService(
            claimService,
            documentManagementService,
            sealedClaimPdfService,
            claimIssueReceiptService,
            defendantResponseReceiptService,
            settlementAgreementCopyService,
            reviewOrderService,
            claimantDirectionsQuestionnairePdfService,
            userService
        );
        when(userService.getUser(AUTHORISATION)).thenReturn(CLAIMANT);
    }

    @Test
    public void shouldGetOCON9xForm() {

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        ScannedDocument oconDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype(ScannedDocumentSubtype.OCON9X.value)
            .build();
        claimDocumentCollection.addScannedDocument(oconDocument);
        Claim claim = SampleClaim.getDefault().toBuilder().claimDocumentCollection(claimDocumentCollection).build();

        when(userService.getUser(AUTHORISATION)).thenReturn(DEFENDANT);
        when(claimService.getClaimByExternalId(claim.getExternalId(), DEFENDANT)).thenReturn(claim);

        when(documentManagementService.downloadScannedDocument(AUTHORISATION, oconDocument))
            .thenReturn(PDF_BYTES);

        byte[] pdf = documentManagementBackendDocumentsService.generateScannedDocument(claim.getExternalId(),
            FORM, OCON9X,  AUTHORISATION);

        assertArrayEquals(PDF_BYTES, pdf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfErrorGettingOCON9xForm() {

        when(userService.getUser(AUTHORISATION)).thenReturn(DEFENDANT);
        Claim claimWithoutOCON9xForm = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(claimWithoutOCON9xForm.getExternalId(), DEFENDANT))
            .thenReturn(claimWithoutOCON9xForm);

        documentManagementBackendDocumentsService.generateScannedDocument(claimWithoutOCON9xForm.getExternalId(),
            FORM, OCON9X, AUTHORISATION);
    }

    @Test
    public void shouldGenerateSealedClaim() {
        when(userService.getUser(AUTHORISATION)).thenReturn(DEFENDANT);
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(DEFENDANT)))
            .thenReturn(claim);
        when(sealedClaimPdfService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "sealedClaim",
            PDF_BYTES,
            SEALED_CLAIM
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    private Claim getClaimWithDocuments() {
        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        claimDocumentCollection.addClaimDocument(builder().id("12345").build());
        return SampleClaim.getDefault().toBuilder().claimDocumentCollection(claimDocumentCollection).build();
    }

    @Test
    public void shouldGenerateGeneralLetter() {
        when(userService.getUser(AUTHORISATION)).thenReturn(DEFENDANT);
        Claim claim = getClaimWithDocuments();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(DEFENDANT))).thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(claim.getExternalId(), GENERAL_LETTER,
            "12345", AUTHORISATION);
        assertArrayEquals(PDF_BYTES, pdf);
    }

    @Test(expected = ForbiddenActionException.class)
    public void throwForbiddenWhenClaimantDownloadSealedClaim() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        documentManagementBackendDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
    }

    @Test
    public void shouldGenerateClaimIssueReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(claimIssueReceiptService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "claimIssueReceipt",
            PDF_BYTES,
            CLAIM_ISSUE_RECEIPT
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
            claim.getExternalId(),
            CLAIM_ISSUE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateDefendantResponseReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(defendantResponseReceiptService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "defendantResponseReceipt",
            PDF_BYTES,
            DEFENDANT_RESPONSE_RECEIPT
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
            claim.getExternalId(),
            DEFENDANT_RESPONSE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldGenerateSettlementAgreement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(settlementAgreementCopyService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "settlementAgreementCopy",
            PDF_BYTES,
            SETTLEMENT_AGREEMENT
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
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
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(reviewOrderService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "reviewOrder",
            PDF_BYTES,
            REVIEW_ORDER
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
            claim.getExternalId(),
            REVIEW_ORDER,
            AUTHORISATION);
        verifyCommon(pdf);
    }

    @Test
    public void shouldNotUploadDocumentIfItAlreadyExists() {
        when(userService.getUser(AUTHORISATION)).thenReturn(DEFENDANT);
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(DEFENDANT)))
            .thenReturn(claim);

        when(documentManagementService.downloadDocument(eq(AUTHORISATION), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);

        documentManagementBackendDocumentsService.generateDocument(
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

    @Test
    public void shouldGenerateClaimantHearingRequirement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(claimantDirectionsQuestionnairePdfService.createPdf(any(Claim.class))).thenReturn(new PDF(
            "claimantDirectionsQuestionnaire",
            PDF_BYTES,
            CLAIMANT_DIRECTIONS_QUESTIONNAIRE
        ));
        byte[] pdf = documentManagementBackendDocumentsService.generateDocument(
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
            .submitterId(CLAIMANT.getUserDetails().getId())
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackendDocumentsService.generateDocument(
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
            .submitterId(CLAIMANT.getUserDetails().getId())
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackendDocumentsService.generateDocument(
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
            .submitterId(CLAIMANT.getUserDetails().getId())
            .claimDocumentCollection(claimDocumentCollection)
            .build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(CLAIMANT)))
            .thenReturn(claim);
        when(documentManagementService.downloadDocument(eq(AUTHORISATION), eq(claimDocument))).thenReturn(new byte[1]);
        documentManagementBackendDocumentsService.generateDocument(
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
