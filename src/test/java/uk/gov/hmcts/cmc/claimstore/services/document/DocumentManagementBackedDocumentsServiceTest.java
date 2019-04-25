package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
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

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementBackedDocumentsServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    private DocumentManagementBackedDocumentsService documentManagementBackedDocumentsService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
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
            defendantPinLetterPdfService);
    }

    @Test
    public void shouldGenerateSealedClaim() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(sealedClaimPdfService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateSealedClaim(claim.getExternalId(), AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldGenerateClaimIssueReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(claimIssueReceiptService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateClaimIssueReceipt(
            claim.getExternalId(),
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldGenerateDefendantResponseReceipt() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(defendantResponseReceiptService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateDefendantResponseReceipt(
            claim.getExternalId(),
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldThrowErrorWhenDefendantResponseDoesNotExist() {
        Claim claim = SampleClaim.builder().build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Defendant response does not exist for this claim");
        documentManagementBackedDocumentsService.generateDefendantResponseReceipt(
            claim.getExternalId(),
            AUTHORISATION);
    }

    @Test
    public void shouldGenerateCountyCourtJudgement() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(countyCourtJudgmentPdfService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateCountyCourtJudgement(
            claim.getExternalId(),
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldThrowErrorWhenCountyCourtJudgementDoesNotExist() {
        Claim claim = SampleClaim.withFullClaimData();
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("County Court Judgment does not exist for this claim");
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        documentManagementBackedDocumentsService.generateCountyCourtJudgement(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldGenerateSettlementAgreement() {
        Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(settlementAgreementCopyService.createPdf(any(Claim.class))).thenReturn(PDF_BYTES);
        byte[] pdf = documentManagementBackedDocumentsService.generateSettlementAgreement(
            claim.getExternalId(),
            AUTHORISATION);
        verifyCommon(pdf, claim.getId());
    }

    @Test
    public void shouldThrowErrorWhenSettlementDoesNotExist() {
        Claim claim = SampleClaim.getDefault();
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Settlement Agreement does not exist for this claim");
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        documentManagementBackedDocumentsService.generateSettlementAgreement(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldGenerateDefendantPinLetter() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(defendantPinLetterPdfService.createPdf(any(Claim.class), anyString())).thenReturn(PDF_BYTES);
        documentManagementBackedDocumentsService.generateDefendantPinLetter(
            claim.getExternalId(),
            "pin",
            AUTHORISATION);
        verify(documentManagementService).uploadDocument(anyString(), any(PDF.class));
    }

    @Test
    public void shouldNotUploadDocumentIfItAlreadyExists() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        documentManagementBackedDocumentsService.generateSealedClaim(claim.getExternalId(), AUTHORISATION);
        verify(documentManagementService, atLeastOnce()).downloadDocument(anyString(), any(URI.class), anyString());
        verify(documentManagementService, never()).uploadDocument(anyString(), any(PDF.class));
        verify(claimService, never()).saveClaimDocuments(
            eq(AUTHORISATION),
            eq(claim.getId()),
            any(ClaimDocumentCollection.class),
            any(ClaimDocumentType.class));
    }

    private void verifyCommon(byte[] pdf, Long claimId) {
        assertEquals(PDF_BYTES, pdf);
        verify(documentManagementService).uploadDocument(anyString(), any(PDF.class));
    }
}
