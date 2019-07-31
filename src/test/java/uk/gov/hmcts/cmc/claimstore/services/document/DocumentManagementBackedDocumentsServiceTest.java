package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
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
    private CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    @Mock
    private SettlementAgreementCopyService settlementAgreementCopyService;
    @Mock
    private DefendantPinLetterPdfService defendantPinLetterPdfService;
    @Mock
    private CCDEventProducer ccdEventProducer;

    @Before
    public void setUp() {
        documentManagementBackedDocumentsService = new DocumentManagementBackedDocumentsService(
            claimService,
            documentManagementService,
            sealedClaimPdfService,
            claimIssueReceiptService,
            defendantResponseReceiptService,
            countyCourtJudgmentPdfService,
            settlementAgreementCopyService,
            defendantPinLetterPdfService,
            ccdEventProducer);
    }

    @Test
    public void shouldGenerateSealedClaim() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(sealedClaimPdfService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldGenerateClaimIssueReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(claimIssueReceiptService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            CLAIM_ISSUE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldGenerateDefendantResponseReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(defendantResponseReceiptService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            DEFENDANT_RESPONSE_RECEIPT,
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldGenerateSettlementAgreement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(settlementAgreementCopyService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SETTLEMENT_AGREEMENT,
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldNotUploadDocumentIfItAlreadyExists() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(sealedClaimPdfService.filename(claim)).thenReturn("filename");
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        documentManagementBackedDocumentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION);
        verify(documentManagementService, atLeastOnce()).downloadDocument(
            eq(AUTHORISATION),
            any(URI.class),
            eq("filename"));
        verify(documentManagementService, never()).uploadDocument(anyString(), any(PDF.class));
        verify(claimService, never()).saveClaimDocuments(
            eq(AUTHORISATION),
            eq(claim.getId()),
            any(ClaimDocumentCollection.class),
            any(ClaimDocumentType.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowIfDocumentTypeDoesNotHaveAService() {
        documentManagementBackedDocumentsService.generateDocument(
            "1234",
            CCJ_REQUEST,
            AUTHORISATION);
    }

    private void verifyCommon(byte[] pdf, Long claimId) {
        assertEquals(PDF_BYTES, pdf);
        verify(documentManagementService).uploadDocument(anyString(), any(PDF.class));
    }
}
