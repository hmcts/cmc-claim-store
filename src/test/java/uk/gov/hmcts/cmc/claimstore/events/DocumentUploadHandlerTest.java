package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private String submitterName = "Dr. John Smith";
    private String pin = "123456";
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;
    @Mock
    private CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    @Mock
    private SettlementAgreementCopyService settlementAgreementCopyService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;
    @Mock
    private DefendantPinLetterPdfService defendantPinLetterPdfService;
    @Mock
    private DocumentsService documentService;

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
        new AgreementCountersignedEvent(SampleClaim.getDefault(),
            MadeBy.CLAIMANT,
            AUTHORISATION);
    private final CountersignSettlementAgreementEvent countersignSettlementAgreementEvent =
        new CountersignSettlementAgreementEvent(SampleClaim.builder().withSettlement(mock(Settlement.class)).build(),
            AUTHORISATION);

    @Before
    public void setUp() {
        documentUploadHandler = new DocumentUploadHandler(publisher,
            defendantResponseReceiptService,
            countyCourtJudgmentPdfService,
            settlementAgreementCopyService,
            sealedClaimPdfService,
            claimIssueReceiptService,
            defendantPinLetterPdfService,
            documentService);
    }

    @Test
    public void citizenClaimIssuedEventTriggersDocumentUpload() {
        Claim claim = SampleClaim.getDefault();
        CitizenClaimIssuedEvent event = new CitizenClaimIssuedEvent(claim, pin, submitterName, AUTHORISATION);
        documentUploadHandler.uploadDocument(event);
        verify(documentService, times(3))
            .uploadToDocumentManagement(argumentCaptor.capture(), anyString(), any(Claim.class));
        List<PDF> capturedDocuments = argumentCaptor.getAllValues();
        List<ClaimDocumentType> expectedClaimDocumentTypes = Arrays.asList(SEALED_CLAIM,
            DEFENDANT_PIN_LETTER,
            CLAIM_ISSUE_RECEIPT);
        capturedDocuments.forEach(document ->
            assertTrue(expectedClaimDocumentTypes.contains(document.getClaimDocumentType())));
    }

    @Test
    public void citizenClaimIssuedEventThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new CitizenClaimIssuedEvent(null, pin, submitterName, AUTHORISATION));
    }

    @Test
    public void representedClaimIssuedEventTriggersDocumentUpload() {
        Claim claim = SampleClaim.getDefault();
        RepresentedClaimIssuedEvent event = new RepresentedClaimIssuedEvent(claim, submitterName, AUTHORISATION);
        documentUploadHandler.uploadDocument(event);
        assertCommon(SEALED_CLAIM);
    }

    @Test
    public void representedClaimIssuedEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new RepresentedClaimIssuedEvent(null, submitterName, AUTHORISATION));
    }

    @Test
    public void defendantResponseEventTriggersDocumentUpload() {
        documentUploadHandler.uploadDocument(defendantResponseEvent);
        assertCommon(DEFENDANT_RESPONSE_RECEIPT);
    }

    @Test
    public void defendantResponseEventForDocumentUploadThrowsExceptionWhenResponseNotPresent() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Response must be present");
        documentUploadHandler.uploadDocument(defendantResponseEventWithoutResponse);
    }

    @Test
    public void defendantResponseEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new DefendantResponseEvent(null, AUTHORISATION));
    }

    @Test
    public void countyCourtJudgmentEventTriggersDocumentUpload() {
        documentUploadHandler.uploadDocument(ccjWithoutAdmission);
        assertCommon(CCJ_REQUEST);
    }

    @Test
    public void countyCourtJudgmentEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new CountyCourtJudgmentEvent(null, AUTHORISATION));
    }

    @Test
    public void agreementCountersignedEventShouldTriggersDocumentUpload() {
        documentUploadHandler.uploadDocument(offerMadeByClaimant);
        assertCommon(SETTLEMENT_AGREEMENT);
    }

    @Test
    public void agreementCountersignedEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new AgreementCountersignedEvent(null, null, AUTHORISATION));
    }

    @Test
    public void countersignSettlementAgreementEventShouldTriggersDocumentUpload() {
        documentUploadHandler.uploadDocument(countersignSettlementAgreementEvent);
        assertCommon(SETTLEMENT_AGREEMENT);
    }

    @Test
    public void countersignSettlementAgreementEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new CountersignSettlementAgreementEvent(null, AUTHORISATION));
    }

    private void assertCommon(ClaimDocumentType claimDocumentType) {
        verify(documentService, times(1))
            .uploadToDocumentManagement(argumentCaptor.capture(), anyString(), any(Claim.class));
        assertTrue(argumentCaptor.getValue().getClaimDocumentType() == claimDocumentType);
    }
}
