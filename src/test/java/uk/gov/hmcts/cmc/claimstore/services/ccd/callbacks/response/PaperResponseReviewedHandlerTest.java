package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response.PaperResponseReviewedHandler.CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response.PaperResponseReviewedHandler.DEFENDANT;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.BUSINESS_QUEUE;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.FORM;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.withFullClaimData;

@ExtendWith(MockitoExtension.class)
class PaperResponseReviewedHandlerTest {

    @InjectMocks
    PaperResponseReviewedHandler paperResponseReviewedHandler;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private EmailTemplates mailTemplates;

    @Mock
    private MoreTimeRequestRule moreTimeRequestRule;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;

    @Mock
    private NotificationTemplates notificationTemplates;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    private final CaseDetails detailsBeforeEvent = CaseDetails.builder().id(1L).build();
    private final CaseDetails detailsAfterEvent = CaseDetails.builder().id(2L).build();

    private CallbackRequest callbackRequest = CallbackRequest.builder()
        .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
        .caseDetails(CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build()).build();

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private ClaimDocumentCollection documentCollection;
    private ClaimDocumentCollection documentCollectionAfter;

    private Claim claim = withFullClaimData().toBuilder()
        .claimDocumentCollection(documentCollection)
        .build();

    @BeforeEach
    public void setUp() {
        documentCollection = new ClaimDocumentCollection();
        documentCollectionAfter = new ClaimDocumentCollection();

        callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
            .caseDetails(CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build()).build();

        lenient().when(notificationsProperties.getFrontendBaseUrl()).thenReturn("http://frontend.url");
        lenient().when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
        lenient().when(notificationTemplates.getEmail()).thenReturn(mailTemplates);

        lenient().when(mailTemplates.getPaperResponseFormReceived()).thenReturn("Template1");
        lenient().when(mailTemplates.getPaperResponseReceivedAndCaseTransferredToCCBC()).thenReturn("Template2");
        lenient().when(mailTemplates.getPaperResponseFromClaimantCaseHandoverToCCBC()).thenReturn("Template3");
        lenient().when(mailTemplates.getPaperResponseFromDefendantCaseHandoverToCCBC()).thenReturn("Template4");
        lenient().when(mailTemplates.getPaperResponseFromClaimantGeneralLetter()).thenReturn("Template5");
        lenient().when(mailTemplates.getPaperResponseFromDefendantGeneralLetter()).thenReturn("Template6");
        lenient().when(mailTemplates.getPaperResponseFormReceivedForCcjRequest()).thenReturn("Template7");

        lenient().when(launchDarklyClient.isFeatureEnabled("paper-response-review-new-handling")).thenReturn(true);
    }

    @Test
    void failsWhenDuplicateMoreTimeRequestedComesThrough() {
        documentCollection.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        verifyDuplicateMoreTimeRequestFails();
    }

    @Test
    void failsWhenDuplicateMoreTimeRequestedThroughStaffUpload() {
        documentCollection.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addScannedDocument(ScannedDocument.builder().subtype("N9").build());
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().documentType(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME).build());

        verifyDuplicateMoreTimeRequestFails();
    }

    private void verifyDuplicateMoreTimeRequestFails() {
        claim = withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
        Claim claimAfterEvent = withFullClaimData().toBuilder()
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

        var response = (AboutToStartOrSubmitCallbackResponse) paperResponseReviewedHandler.handle(callbackParams);
        assertThat(response.getErrors())
            .contains("Requesting More Time to respond can be done only once.");
    }

    @Test
    void verifyMoreTimeRequestedIsHandledByScannedDocumentUpload() {
        AboutToStartOrSubmitCallbackResponse response = verifyMailWithCorrectTemplateIsSent(FORM, "N9",
            "Template1", SUBMITTER_EMAIL, false, 1);

        verify(caseMapper).to(claimArgumentCaptor.capture());
        assertThat(claimArgumentCaptor.getValue().isMoreTimeRequested())
            .isTrue();
        assertThat(claimArgumentCaptor.getValue().getResponseDeadline())
            .isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void verifyClaimMovesToBusinessQueueStateAfterPaperResponseIsReviewed() {
        verifyClaimState("N9a", 1);
        verifyClaimState("N9b", 2);
        verifyClaimState("N11", 3);
    }

    private void verifyClaimState(String subType, int timesCalled) {
        AboutToStartOrSubmitCallbackResponse response = verifyMailWithCorrectTemplateIsSent(FORM, subType,
            "Template2", SUBMITTER_EMAIL, false, timesCalled);
        assertEquals(BUSINESS_QUEUE.getValue(), response.getState());
    }

    @Test
    public void shouldNotUseNewEmailTemplatesIfFeatureTurnedOff() {
        when(launchDarklyClient.isFeatureEnabled("paper-response-review-new-handling")).thenReturn(false);
        verifyMailWithCorrectTemplateIsSent(FORM, "N9a", "Template3", SUBMITTER_EMAIL, false, 0);
    }

    @Test
    public void shouldTriggerMailWithSpecificMailTemplateForTheProvidedScannedDocument() {

        verifyMailWithCorrectTemplateIsSent(FORM, "N9a", "Template2", SUBMITTER_EMAIL, false, 1);
        verifyMailWithCorrectTemplateIsSent(FORM, "N9b", "Template2", SUBMITTER_EMAIL, false, 2);
        verifyMailWithCorrectTemplateIsSent(FORM, "N11", "Template2", SUBMITTER_EMAIL, false, 3);

        verifyMailWithCorrectTemplateIsSent(FORM, "N180", "Template4", SUBMITTER_EMAIL, false, 1);
        verifyMailWithCorrectTemplateIsSent(FORM, "EX160", "Template4", SUBMITTER_EMAIL, false, 2);
        verifyMailWithCorrectTemplateIsSent(FORM, "N244", "Template4", SUBMITTER_EMAIL, false, 3);
        verifyMailWithCorrectTemplateIsSent(FORM, "N245", "Template4", SUBMITTER_EMAIL, false, 4);
        verifyMailWithCorrectTemplateIsSent(FORM, "Non_prescribed_documents", "Template4", SUBMITTER_EMAIL, false, 5);

        verifyMailWithCorrectTemplateIsSent(FORM, "N180", "Template3", DEFENDANT_EMAIL, true, 1);
        verifyMailWithCorrectTemplateIsSent(FORM, "EX160", "Template3", DEFENDANT_EMAIL, true, 2);
        verifyMailWithCorrectTemplateIsSent(FORM, "N244", "Template3", DEFENDANT_EMAIL, true, 3);
        verifyMailWithCorrectTemplateIsSent(FORM, "N245", "Template3", DEFENDANT_EMAIL, true, 4);
        verifyMailWithCorrectTemplateIsSent(FORM, "Non_prescribed_documents", "Template3", DEFENDANT_EMAIL, true, 5);

        verifyMailWithCorrectTemplateIsSent(FORM, "N225", "Template7", SUBMITTER_EMAIL, true, 1);

        verifyMailWithCorrectTemplateIsSent(ScannedDocumentType.OTHER, "abc", "Template6", DEFENDANT_EMAIL, false, 1);
        verifyMailWithCorrectTemplateIsSent(ScannedDocumentType.LETTER, "xyz", "Template6", DEFENDANT_EMAIL, false, 2);

        verifyMailWithCorrectTemplateIsSent(ScannedDocumentType.OTHER, "abc", "Template5", CLAIMANT_EMAIL, true, 1);
        verifyMailWithCorrectTemplateIsSent(ScannedDocumentType.LETTER, "xyz", "Template5", CLAIMANT_EMAIL, true, 2);

        verifyMailWithCorrectTemplateIsSent(ScannedDocumentType.COVERSHEET, "", "Template1", SUBMITTER_EMAIL, false, 1);
    }

    private AboutToStartOrSubmitCallbackResponse verifyMailWithCorrectTemplateIsSent(ScannedDocumentType docType,
        String subType, String expectedTemplate, String expectedEmail, Boolean submittedByClaimant, int timesCalled) {

        documentCollectionAfter = new ClaimDocumentCollection();
        documentCollectionAfter.addScannedDocument(ScannedDocument.builder()
            .documentType(docType).subtype(subType).submittedBy(submittedByClaimant ? CLAIMANT : DEFENDANT).build());
        Claim afterClaim = withFullClaimData().toBuilder().claimDocumentCollection(documentCollectionAfter).build();
        Claim beforeClaim = withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();

        when(caseDetailsConverter.extractClaim(detailsAfterEvent)).thenReturn(afterClaim);
        when(caseDetailsConverter.extractClaim(detailsBeforeEvent)).thenReturn(beforeClaim);
        LocalDate newResponseDeadline = LocalDate.now().plusDays(7);
        lenient().when(responseDeadlineCalculator.calculatePostponedResponseDeadline(afterClaim.getIssuedOn()))
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

        final AboutToStartOrSubmitCallbackResponse response = paperResponseReviewedHandler.handle(callbackParams);

        String party = SUBMITTER_EMAIL.equals(expectedEmail) ? CLAIMANT : DEFENDANT;

        verify(notificationService, times(timesCalled))
            .sendMail(
                eq(expectedEmail),
                eq(expectedTemplate),
                anyMap(),
                eq("paper-response-submitted-" + party + "-" + REFERENCE_NUMBER));

        return response;
    }

    @Test
    void verifyResponseByStaffUploadedDocumentIsHandled() {
        final LocalDateTime docReceivedTime = LocalDateTime.now();
        documentCollectionAfter.addStaffUploadedDocument(
            ClaimDocument.builder().id("SP")
                .documentType(ClaimDocumentType.PAPER_RESPONSE_STATES_PAID)
                .receivedDateTime(docReceivedTime)
                .build()
        );

        claim = withFullClaimData().toBuilder().claimDocumentCollection(documentCollection).build();
        Claim claimAfterEvent = withFullClaimData().toBuilder()
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

        paperResponseReviewedHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());
        assertThat(claimArgumentCaptor.getValue().getRespondedAt())
            .isEqualTo(docReceivedTime);

        verify(notificationService)
            .sendMail(
                eq(SUBMITTER_EMAIL),
                eq("Template1"),
                anyMap(),
                eq("paper-response-submitted-claimant-" + REFERENCE_NUMBER));
    }
}
