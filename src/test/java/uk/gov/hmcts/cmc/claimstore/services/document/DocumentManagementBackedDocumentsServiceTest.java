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
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementBackedDocumentsServiceTest {

    private static final String CLAIMREFERENCENUMBER = "000CM001";
    private static final String AUTHORISATION = "Bearer: aaa";
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
    @Mock
    private UserService userService;

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
            userService);
    }

    @Test
    public void shouldGenerateDefendantPinLetter() {
        Claim claim = SampleClaim.getWithDefendantPinLetterDocument(0);
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(userService.generatePin(any(), eq(AUTHORISATION)))
            .thenReturn(new GeneratePinResponse("my-pin", "2"));
        documentManagementBackedDocumentsService.generateDefendantPinLetter(claim.getExternalId(), AUTHORISATION);
        verify(documentManagementService).uploadDocument(anyString(), any(PDF.class));
    }

    @Test
    public void shouldThrowErrorForDefendantPinLetterWhenDefendantLinkedAlready() {
        Claim claim = SampleClaim.getWithDefendantResponseReceiptDocument(0);
        when(claimService.getClaimByExternalId(eq(claim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);
        exceptionRule.expect(ConflictException.class);
        exceptionRule.expectMessage("Cannot generate pin letter.Claim has already been linked to defendant.");
        documentManagementBackedDocumentsService.generateDefendantPinLetter(claim.getExternalId(), AUTHORISATION);
    }
}
