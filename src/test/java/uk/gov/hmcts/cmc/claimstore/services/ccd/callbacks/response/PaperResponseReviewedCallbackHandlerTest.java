package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocument.builder;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.PAPER_RESPONSE_PART_ADMIT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.PAPER_RESPONSE_STATES_PAID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;

@ExtendWith(MockitoExtension.class)
@DisplayName("Paper Response Reviewed handler")
class PaperResponseReviewedCallbackHandlerTest {

    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private MoreTimeRequestRule moreTimeRequestRule;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private NotificationTemplates notificationTemplates;
    @Mock
    LaunchDarklyClient launchDarklyClient;

    private final CaseDetails detailsBeforeEvent = CaseDetails.builder().id(1L).build();
    private final CaseDetails detailsAfterEvent = CaseDetails.builder().id(2L).build();

    private CallbackRequest callbackRequest;

    private Claim claim;

    private PaperResponseReviewedCallbackHandler handler;

    private static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request "
        + "because the defendant already responded to the claim";

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    @BeforeEach
    void setUp() {
        handler = new PaperResponseReviewedCallbackHandler(
            caseDetailsConverter,
            caseMapper,
            responseDeadlineCalculator,
            moreTimeRequestRule,
            notificationService,
            notificationsProperties,
            launchDarklyClient);
        callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
            .caseDetails(CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build()).build();
    }

    @Test
    @DisplayName("should include error when already responded online if LD enabled for restrict-review-paper-response")
    void eventNotPossibleWhenRespondedOnline() {
        when(launchDarklyClient.isFeatureEnabled("restrict-review-paper-response")).thenReturn(true);
        claim = SampleClaim.getWithDefaultResponse();
        checkEventAllowed(claim, true);
    }

    @Test
    @DisplayName("should include error when already responded offline if LD enabled for restrict-review-paper-response")
    void eventNotPossibleWhenRespondedOffline() {
        when(launchDarklyClient.isFeatureEnabled("restrict-review-paper-response")).thenReturn(true);
        claim = SampleClaim.withFullClaimData().toBuilder().respondedAt(LocalDateTime.now()).build();
        checkEventAllowed(claim, true);
    }

    @Test
    @DisplayName("should include error when already responded online if LD enabled for restrict-review-paper-response")
    void eventPossibleWhenRespondedOnlineIfNotRestricted() {
        claim = SampleClaim.getWithDefaultResponse();
        checkEventAllowed(claim, false);
    }

    @Test
    @DisplayName("should include error when already responded offline if LD enabled for restrict-review-paper-response")
    void eventPossibleWhenRespondedOfflineIfNotRestricted() {
        claim = SampleClaim.withFullClaimData().toBuilder().respondedAt(LocalDateTime.now()).build();
        checkEventAllowed(claim, false);
    }

    private void checkEventAllowed(Claim claim, boolean errorExpected) {

        when(caseDetailsConverter.extractClaim(any())).thenReturn(this.claim);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertEquals(errorExpected, response.getErrors() != null
            && response.getErrors().contains(ALREADY_RESPONDED_ERROR));
    }

    @Nested
    @DisplayName("When response is possible")
    class WhenResponseIsPossible {
        ClaimDocumentCollection documentCollection;
        ClaimDocumentCollection documentCollectionAfter;

        @BeforeEach
        void setUp() {
            documentCollection = new ClaimDocumentCollection();
            documentCollectionAfter = new ClaimDocumentCollection();
        }

        @Test
        @DisplayName("fails when duplicate more time request comes through")
        void failsWhenDuplicateMoreTimeRequestedComesThrough() {
            documentCollection.addStaffUploadedDocument(
                builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

            documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
            documentCollectionAfter.addStaffUploadedDocument(
                builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

            claim = SampleClaim.withFullClaimData().toBuilder()
                .claimDocumentCollection(documentCollection)
                .build();
            Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
                .claimDocumentCollection(documentCollectionAfter)
                .build();

            when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(claim);
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

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            assertThat(response.getErrors())
                .contains("Requesting More Time to respond can be done only once.");
        }

        @Test
        @DisplayName("fails when duplicate more time request through staff upload")
        void failsWhenDuplicateMoreTimeRequestedThroughStaffUpload() {
            documentCollection.addScannedDocument(ScannedDocument.builder().subtype("N9").build());

            documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
            documentCollectionAfter.addStaffUploadedDocument(
                builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

            claim = SampleClaim.withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
            Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
                .claimDocumentCollection(documentCollectionAfter).build();

            when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(claim);
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

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            assertThat(response.getErrors())
                .contains("Requesting More Time to respond can be done only once.");
        }

        @Test
        @DisplayName("more time request is handled by scanned document upload and email is sent")
        void verifyMoreTimeRequestedIsHandledByScannedDocumentUploadAndEmailIsSentForN9Form() {
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn("http://frontend.url");
            when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
            when(notificationTemplates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getClaimantPaperResponseReceived()).thenReturn("TEMPLATE");

            verifyMoreTimeRequestedIsHandledByScannedDocumentUpload("N9", 1);

            assertThat(claimArgumentCaptor.getValue().isMoreTimeRequested())
                .isTrue();
            assertThat(claimArgumentCaptor.getValue().getResponseDeadline())
                .isEqualTo(LocalDate.now().plusDays(7));
        }

        @Test
        @DisplayName("email is sent on mediation agreement docuemnt upload by staff")
        void verifyEmailIsSentWhenMediationAgreementIsUploaded() {
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn("http://frontend.url");
            when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
            when(notificationTemplates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getClaimantPaperResponseReceived()).thenReturn("TEMPLATE");

            documentCollectionAfter.addStaffUploadedDocument(builder().documentType(PAPER_RESPONSE_PART_ADMIT).build());

            verifyMoreTimeRequestedIsHandledByScannedDocumentUpload("N225", 1);
        }

        @Test
        @DisplayName("verify that email is not sent for documents other than N9, N9a, N911")
        void verifyMoreTimeRequestedIsHandledByScannedDocumentUploadAndEmailIsNotSentForN225Form() {
            verifyMoreTimeRequestedIsHandledByScannedDocumentUpload("N225", 0);
        }

        void verifyMoreTimeRequestedIsHandledByScannedDocumentUpload(String form, int timesEmailIsSent) {
            ClaimDocument staffUploadedDoc = builder().documentType(PAPER_RESPONSE_STATES_PAID).build();
            documentCollection.addStaffUploadedDocument(staffUploadedDoc);

            documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype(form).build());
            documentCollectionAfter.addStaffUploadedDocument(staffUploadedDoc);

            claim = SampleClaim.withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
            Claim claimAfterEvent = SampleClaim.withFullClaimData().toBuilder()
                .claimDocumentCollection(documentCollectionAfter).build();

            when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(claimAfterEvent);
            when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(claim);
            LocalDate newResponseDeadline = LocalDate.now().plusDays(7);
            lenient().when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claimAfterEvent.getIssuedOn()))
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

            handler.handle(callbackParams);

            verify(caseMapper).to(claimArgumentCaptor.capture());
            verify(notificationService, times(timesEmailIsSent))
                .sendMail(
                    eq(SUBMITTER_EMAIL),
                    eq("TEMPLATE"),
                    anyMap(),
                    eq("paper-response-submitted-claimant-" + REFERENCE_NUMBER));
        }

        @Test
        @DisplayName("response by staff uploaded document is handled")
        void verifyResponseByStaffUploadedDocumentIsHandled() {
            lenient().when(notificationsProperties.getFrontendBaseUrl()).thenReturn("http://frontend.url");
            lenient().when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
            lenient().when(notificationTemplates.getEmail()).thenReturn(emailTemplates);
            lenient().when(emailTemplates.getClaimantPaperResponseReceived()).thenReturn("TEMPLATE");

            documentCollection.addScannedDocument(ScannedDocument.builder().id("N9").subtype("N9").build());
            documentCollectionAfter.addScannedDocument(ScannedDocument.builder().id("N9").subtype("N9").build());
            final LocalDateTime docReceivedTime = LocalDateTime.now();
            documentCollectionAfter.addStaffUploadedDocument(
                builder().id("SP")
                    .documentType(PAPER_RESPONSE_STATES_PAID)
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

            handler.handle(callbackParams);

            verify(caseMapper).to(claimArgumentCaptor.capture());
            assertThat(claimArgumentCaptor.getValue().getRespondedAt())
                .isEqualTo(docReceivedTime);

            verify(notificationService)
                .sendMail(
                    eq(SUBMITTER_EMAIL),
                    eq("TEMPLATE"),
                    anyMap(),
                    eq("paper-response-submitted-claimant-" + REFERENCE_NUMBER));
        }
    }
}
