package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaperResponseReviewedCallbackHandlerTest {

    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private MoreTimeRequestRule moreTimeRequestRule;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;

    private CallbackRequest callbackRequest;

    private Claim claim;

    private PaperResponseReviewedCallbackHandler handlerToTest;

    private static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request "
        + "because the defendant already responded to the claim";

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private final CaseDetails detailsBeforeEvent = CaseDetails.builder().id(1L).build();
    private final CaseDetails detailsAfterEvent = CaseDetails.builder().id(2L).build();

    @Before
    public void setUp() {
        handlerToTest = new PaperResponseReviewedCallbackHandler(caseDetailsConverter,
            caseMapper,
            responseDeadlineCalculator,
            moreTimeRequestRule);
        callbackRequest = CallbackRequest.builder().eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
            .caseDetails(CaseDetails
                .builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build())
            .build();
    }

    @Test
    public void eventNotPossibleWhenRespondedOnline() {
        claim = SampleClaim.getWithDefaultResponse();
        when(caseDetailsConverter.extractClaim(any())).thenReturn(claim);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handlerToTest.handle(callbackParams);

        assertThat(response.getErrors()).contains(ALREADY_RESPONDED_ERROR);
    }

    @Test
    public void eventNotPossibleWhenRespondedOffline() {
        claim = SampleClaim.withFullClaimData()
            .toBuilder()
            .respondedAt(LocalDateTime.now()).build();

        when(caseDetailsConverter.extractClaim(any())).thenReturn(claim);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handlerToTest.handle(callbackParams);

        assertThat(response.getErrors()).contains(ALREADY_RESPONDED_ERROR);
    }

    @Test
    public void failsWhenDuplicateMoreTimeRequestedComesThru() {
        ClaimDocumentCollection documentCollectionAfter = new ClaimDocumentCollection();
        ClaimDocumentCollection documentCollection = new ClaimDocumentCollection();

        documentCollection.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        claim = SampleClaim.withFullClaimData().toBuilder()
            .claimDocumentCollection(documentCollection)
            .build();
        Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
            .claimDocumentCollection(documentCollectionAfter)
            .build();

        when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(claimAfterEvent);

        callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(detailsBeforeEvent)
            .caseDetails(detailsAfterEvent)
            .eventId(CaseEvent.REVIEWED_PAPER_RESPONSE.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handlerToTest
                .handle(callbackParams);

        assertThat(response.getErrors()).contains("Requesting More Time to respond can be done only once.");
    }

    @Test
    public void failsWhenDuplicateMoreTimeRequestedThruStaffUpload() {
        ClaimDocumentCollection documentCollectionAfter = new ClaimDocumentCollection();
        ClaimDocumentCollection documentCollection = new ClaimDocumentCollection();

        documentCollection.addScannedDocument(ScannedDocument.builder().subtype("N9").build());

        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        claim = SampleClaim.withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
        Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
            .claimDocumentCollection(documentCollectionAfter).build();

        when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(claimAfterEvent);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetailsBefore(detailsBeforeEvent)
            .caseDetails(detailsAfterEvent)
            .eventId(CaseEvent.REVIEWED_PAPER_RESPONSE.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handlerToTest
                .handle(callbackParams);

        assertThat(response.getErrors()).contains("Requesting More Time to respond can be done only once.");
    }

    @Test
    public void verifyMoreTimeRequestedIsHandledByScannedDocumentUpload() {
        ClaimDocumentCollection documentCollectionAfter = new ClaimDocumentCollection();
        ClaimDocumentCollection documentCollection = new ClaimDocumentCollection();

        documentCollection.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_STATES_PAID).build());

        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_STATES_PAID).build());

        claim = SampleClaim.withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
        Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
            .claimDocumentCollection(documentCollectionAfter).build();

        when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(claimAfterEvent);
        when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(claim);
        LocalDate newResponseDeadline = LocalDate.now().plusDays(7);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claimAfterEvent.getIssuedOn()))
            .thenReturn(newResponseDeadline);

        callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(detailsBeforeEvent)
            .caseDetails(detailsAfterEvent)
            .eventId(CaseEvent.REVIEWED_PAPER_RESPONSE.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

        handlerToTest
                .handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());
        assertTrue(claimArgumentCaptor.getValue()
            .isMoreTimeRequested());
        assertThat(claimArgumentCaptor.getValue()
            .getResponseDeadline()
            .equals(newResponseDeadline));

    }

    @Test
    public void verifyResponseByStaffUploadedDocumentIsHandled() {
        ClaimDocumentCollection documentCollectionAfter = new ClaimDocumentCollection();
        ClaimDocumentCollection documentCollection = new ClaimDocumentCollection();

        documentCollection.addScannedDocument(ScannedDocument.builder().id("N9").subtype("N9").build());
        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().id("N9").subtype("N9").build());
        final LocalDateTime docReceivedTime = LocalDateTime.now();
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().id("SP")
                .documentType(ClaimDocumentType.PAPER_RESPONSE_STATES_PAID)
                .receivedDateTime(docReceivedTime)
                .build()
        );

        claim = SampleClaim.withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
        Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
            .claimDocumentCollection(documentCollectionAfter).build();

        when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(claimAfterEvent);
        when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetailsBefore(detailsBeforeEvent)
            .caseDetails(detailsAfterEvent)
            .eventId(CaseEvent.REVIEWED_PAPER_RESPONSE.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

        handlerToTest.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());
        assertThat(claimArgumentCaptor.getValue().getRespondedAt().equals(docReceivedTime));
    }
}
