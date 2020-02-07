package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.ReviewOrderService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.documents.questionnaire.ClaimantDirectionsQuestionnairePdfService;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.revieworder.ReviewOrderEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIM_MUST_NOT_BE_NULL = "Claim must not be null";

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;
    @Mock
    private SettlementAgreementCopyService settlementAgreementCopyService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;
    @Mock
    private DefendantPinLetterPdfService defendantPinLetterPdfService;
    @Mock
    private ReviewOrderService reviewOrderService;
    @Mock
    private DocumentsService documentService;
    @Mock
    private ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;

    private DocumentUploadHandler documentUploadHandler;

    private final ArgumentCaptor<PDF> argumentCaptor = ArgumentCaptor.forClass(PDF.class);
    private final DefendantResponseEvent defendantResponseEvent = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE,
        AUTHORISATION
    );
    private final DefendantResponseEvent defendantResponseEventWithoutResponse = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_NO_RESPONSE,
        AUTHORISATION
    );
    private final CountyCourtJudgmentEvent ccjWithoutAdmission = new CountyCourtJudgmentEvent(
        SampleClaimIssuedEvent.CLAIM_WITH_DEFAULT_CCJ,
        AUTHORISATION
    );
    private final AgreementCountersignedEvent offerMadeByClaimant =
        new AgreementCountersignedEvent(SampleClaim.getWithSettlement(SampleSettlement.validDefaults()),
            MadeBy.CLAIMANT,
            AUTHORISATION);
    private final CountersignSettlementAgreementEvent countersignSettlementAgreementEvent =
        new CountersignSettlementAgreementEvent(SampleClaim.builder().withSettlement(mock(Settlement.class)).build(),
            AUTHORISATION);

    private final ReviewOrderEvent reviewOrderEvent = new ReviewOrderEvent(
        AUTHORISATION,
        SampleClaim.builder().withReviewOrder(SampleReviewOrder.getDefault()).build()
    );

    @Before
    public void setUp() {
        documentUploadHandler = new DocumentUploadHandler(
            defendantResponseReceiptService,
            settlementAgreementCopyService,
            claimIssueReceiptService,
            documentService,
            reviewOrderService,
            claimantDirectionsQuestionnairePdfService);
    }

    @Test
    public void citizenClaimIssuedEventTriggersDocumentUpload() {
        Claim claim = SampleClaim.getDefault();
        String referenceNumber = claim.getReferenceNumber();
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(referenceNumber), PDF_CONTENT, SEALED_CLAIM);
        PDF pinLetter = new PDF(buildDefendantLetterFileBaseName(referenceNumber), PDF_CONTENT, DEFENDANT_PIN_LETTER);
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(claim, AUTHORISATION, sealedClaim, pinLetter);

        when(claimIssueReceiptService.createPdf(claim)).thenReturn(new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIM_ISSUE_RECEIPT
        ));

        documentUploadHandler.uploadCitizenClaimDocument(event);

        verify(documentService, times(2))
            .uploadToDocumentManagement(argumentCaptor.capture(), anyString(), any());
        List<PDF> capturedDocuments = argumentCaptor.getAllValues();
        List<ClaimDocumentType> expectedClaimDocumentTypes = Arrays.asList(SEALED_CLAIM,
            CLAIM_ISSUE_RECEIPT);

        List<ClaimDocumentType> actualDocumentTypes = capturedDocuments.stream()
            .map(PDF::getClaimDocumentType)
            .collect(Collectors.toList());

        actualDocumentTypes.forEach(claimDocumentType ->
            assertTrue(expectedClaimDocumentTypes.contains(claimDocumentType)));
    }

    @Test
    public void citizenClaimIssuedEventThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        documentUploadHandler.uploadCitizenClaimDocument(new DocumentGeneratedEvent(null, AUTHORISATION));
    }

    @Test
    public void representedClaimIssuedEventTriggersDocumentUpload() {
        Claim claim = SampleClaim.getLegalDataWithReps();
        String referenceNumber = claim.getReferenceNumber();
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(referenceNumber), PDF_CONTENT, SEALED_CLAIM);
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(claim, AUTHORISATION, sealedClaim);
        documentUploadHandler.uploadCitizenClaimDocument(event);
        assertCommon(SEALED_CLAIM);
    }

    @Test
    public void representedClaimIssuedEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        Claim claim = SampleClaim.getLegalDataWithReps();
        String referenceNumber = claim.getReferenceNumber();
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(referenceNumber), PDF_CONTENT, SEALED_CLAIM);
        documentUploadHandler.uploadCitizenClaimDocument(new DocumentGeneratedEvent(null, AUTHORISATION, sealedClaim));
    }

    @Test
    public void defendantResponseEventTriggersDocumentUpload() {
        PDF defendantResponse = new PDF(buildResponseFileBaseName(
            defendantResponseEvent.getClaim().getReferenceNumber()),
            PDF_CONTENT,
            DEFENDANT_RESPONSE_RECEIPT);
        when(defendantResponseReceiptService.createPdf(defendantResponseEvent.getClaim()))
            .thenReturn(defendantResponse);
        documentUploadHandler.uploadDefendantResponseDocument(defendantResponseEvent);
        assertCommon(DEFENDANT_RESPONSE_RECEIPT);
    }

    @Test
    public void defendantResponseEventForDocumentUploadThrowsExceptionWhenResponseNotPresent() {
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Defendant response does not exist for this claim");
        documentUploadHandler.uploadDefendantResponseDocument(defendantResponseEventWithoutResponse);
    }

    @Test
    public void defendantResponseEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        documentUploadHandler.uploadDefendantResponseDocument(new DefendantResponseEvent(null, AUTHORISATION));
    }

    @Test
    public void agreementCountersignedEventShouldTriggersDocumentUpload() {
        PDF settlementAgreement = new PDF(buildSettlementReachedFileBaseName(
            offerMadeByClaimant.getClaim().getReferenceNumber()),
            PDF_CONTENT,
            SETTLEMENT_AGREEMENT);
        when(settlementAgreementCopyService.createPdf(offerMadeByClaimant.getClaim()))
            .thenReturn(settlementAgreement);
        documentUploadHandler.uploadSettlementAgreementDocument(offerMadeByClaimant);
        assertCommon(SETTLEMENT_AGREEMENT);
    }

    @Test
    public void agreementCountersignedEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        documentUploadHandler.uploadSettlementAgreementDocument(
            new AgreementCountersignedEvent(null, null, AUTHORISATION)
        );
    }

    @Test
    public void agreementCountersignedEventForDocumentUploadThrowsNotFoundExceptionWhenSettlementNotPresent() {
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Settlement Agreement does not exist for this claim");
        documentUploadHandler.uploadSettlementAgreementDocument(
            new AgreementCountersignedEvent(SampleClaim.getDefault(),
                null,
                AUTHORISATION)
        );
    }

    @Test
    public void countersignSettlementAgreementEventShouldTriggersDocumentUpload() {
        PDF settlementAgreement = new PDF(buildSettlementReachedFileBaseName(
            countersignSettlementAgreementEvent.getClaim().getReferenceNumber()),
            PDF_CONTENT,
            SETTLEMENT_AGREEMENT);
        when(settlementAgreementCopyService.createPdf(countersignSettlementAgreementEvent.getClaim()))
            .thenReturn(settlementAgreement);
        documentUploadHandler.uploadSettlementAgreementDocument(countersignSettlementAgreementEvent);
        assertCommon(SETTLEMENT_AGREEMENT);
    }

    @Test
    public void countersignSettlementAgreementEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        documentUploadHandler.uploadSettlementAgreementDocument(
            new CountersignSettlementAgreementEvent(null, AUTHORISATION)
        );
    }

    @Test
    public void claimantAcceptationResponseDoesNothing() {
        documentUploadHandler.uploadClaimantDirectionsQuestionnaireToDM(
            new ClaimantResponseEvent(
                SampleClaim.getWithClaimantResponse(
                    SampleClaimantResponse.validDefaultAcceptation()), AUTHORISATION));

        verifyNoInteractions(claimantDirectionsQuestionnairePdfService);
    }

    @Test
    public void claimantResponseWithoutQuestionnaireDoesNothing() {
        documentUploadHandler.uploadClaimantDirectionsQuestionnaireToDM(
            new ClaimantResponseEvent(
                SampleClaim.getWithClaimantResponse(
                    SampleClaimantResponse.validDefaultRejection()), AUTHORISATION));

        verifyNoInteractions(claimantDirectionsQuestionnairePdfService);
    }

    @Test
    public void claimantResponseWithQuestionnaireUploadsToDM() {
        Claim claim = SampleClaim.getWithClaimantResponse(
            SampleClaimantResponse.ClaimantResponseRejection.builder()
                .buildRejectionWithDirectionsQuestionnaire());

        when(claimantDirectionsQuestionnairePdfService.createPdf(any())).thenReturn(new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIMANT_DIRECTIONS_QUESTIONNAIRE
        ));
        documentUploadHandler.uploadClaimantDirectionsQuestionnaireToDM(
            new ClaimantResponseEvent(claim, AUTHORISATION));
        assertCommon(CLAIMANT_DIRECTIONS_QUESTIONNAIRE);

    }

    private void assertCommon(ClaimDocumentType claimDocumentType) {
        verify(documentService, times(1))
            .uploadToDocumentManagement(argumentCaptor.capture(), anyString(), any(Claim.class));
        assertSame(argumentCaptor.getValue().getClaimDocumentType(), claimDocumentType);
    }

    @Test
    public void reviewOrderEventTriggersDocumentUpload() {
        PDF reviewOrderDocument = new PDF(buildResponseFileBaseName(
            reviewOrderEvent.getClaim().getReferenceNumber()),
            PDF_CONTENT,
            REVIEW_ORDER);
        when(reviewOrderService.createPdf(reviewOrderEvent.getClaim()))
            .thenReturn(reviewOrderDocument);
        documentUploadHandler.uploadReviewOrderRequestDocument(reviewOrderEvent);
        assertCommon(REVIEW_ORDER);
    }

    @Test
    public void reviewOrderEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage(CLAIM_MUST_NOT_BE_NULL);
        documentUploadHandler.uploadReviewOrderRequestDocument(
            new ReviewOrderEvent(AUTHORISATION, null)
        );
    }

    @Test
    public void reviewOrderEventForDocumentUploadThrowsExceptionWhenReviewOrderNotPresent() {
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Review Order does not exist for this claim");
        documentUploadHandler.uploadReviewOrderRequestDocument(
            new ReviewOrderEvent(
                AUTHORISATION,
                SampleClaim.getDefault()
            ));
    }
}
