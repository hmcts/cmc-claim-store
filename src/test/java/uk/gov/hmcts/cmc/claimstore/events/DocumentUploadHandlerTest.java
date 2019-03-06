package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private String submitterName = "Dr. John Smith";
    private String pin = "123456";
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private DefendantPinLetterPdfService defendantPinLetterPdfService;
    @Mock
    private DocumentManagementService documentManagementService;
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
        documentUploadHandler = new DocumentUploadHandler(defendantPinLetterPdfService,
            documentService);
    }

    @Test
    public void citizenClaimIssuedEventTriggersDocumentUpload() {
        Claim claim = SampleClaim.getDefault();
        CitizenClaimIssuedEvent event = new CitizenClaimIssuedEvent(claim, pin, submitterName, AUTHORISATION);
        documentUploadHandler.uploadDocument(event);
        verify(documentService).generateSealedClaim(claim.getExternalId(), AUTHORISATION);
        verify(documentService).generateClaimIssueReceipt(claim.getExternalId(), AUTHORISATION);
        verify(documentService, times(1))
            .uploadToDocumentManagement(argumentCaptor.capture(), anyString(), any(Claim.class));
        PDF document = argumentCaptor.getValue();
        assertTrue(document.getClaimDocumentType() == DEFENDANT_PIN_LETTER);
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
        verify(documentService).generateSealedClaim(eq(claim.getExternalId()), eq(AUTHORISATION));
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
        verify(documentService).generateDefendantResponseReceipt(
            defendantResponseEvent.getClaim().getExternalId(),
            AUTHORISATION);
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
        verify(documentService).generateCountyCourtJudgement(
            ccjWithoutAdmission.getClaim().getExternalId(),
            AUTHORISATION);
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
        verify(documentService).generateSettlementAgreement(
            offerMadeByClaimant.getClaim().getExternalId(),
            AUTHORISATION);
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
        verify(documentService).generateSettlementAgreement(
            offerMadeByClaimant.getClaim().getExternalId(),
            AUTHORISATION);
    }

    @Test
    public void countersignSettlementAgreementEventForDocumentUploadThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must not be null");
        documentUploadHandler.uploadDocument(new CountersignSettlementAgreementEvent(null, AUTHORISATION));
    }

}
