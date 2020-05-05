package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCalculatedResponseDeadline;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.MoreTimeRequestedCallbackHandler.CALCULATED_RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class MoreTimeRequestedCallbackHandlerTest {

    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";
    private static final String CLAIMANT_TEMPLATE_ID = "claimant template id";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final String FRONTEND_BASE_URL = "http://some.host.dot.com";

    private static final String GENERAL_LETTER_TEMPLATE = "generalLetterTemplate";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final String ERROR_MESSAGE = "There was a technical problem. Nothing has been sent."
        + " You need to try again.";
    private static final String AUTHORISATION = "auth";
    private static final LocalDate deadline = LocalDate.now();

    @Mock
    private EventProducer eventProducer;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private MoreTimeRequestRule moreTimeRequestRule;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private GeneralLetterService generalLetterService;
    @Mock
    private UserService userService;

    private Claim claim;

    private CallbackRequest callbackRequest;

    private MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler;

    private CCDCase ccdCase;
    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        moreTimeRequestedCallbackHandler = new MoreTimeRequestedCallbackHandler(
            eventProducer,
            responseDeadlineCalculator,
            moreTimeRequestRule,
            caseDetailsConverter,
            notificationService,
            notificationsProperties,
            generalLetterService,
            userService,
            GENERAL_LETTER_TEMPLATE
        );
        claim = SampleClaim.getDefault();
        claim = Claim.builder()
            .claimData(SampleClaimData.builder().build())
            .defendantEmail("email@email.com")
            .defendantId("id")
            .submitterEmail("email@email.com")
            .referenceNumber("ref. number")
            .build();
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ))
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("general-letter")
                    .build())
                .build()))
            .draftLetterDoc(DRAFT_LETTER_DOC).build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.RESPONSE_MORE_TIME.getValue())
                .caseDetails(caseDetails)
                .build();
    }

    @Nested
    @DisplayName("About to Start Validation test")
    class ValidationTest {
        @BeforeEach
        void setUp() {
            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_START)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();

            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()))
                .thenReturn(deadline);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        }

        @Test
        void shouldValidateRequestOnAboutToStartEvent() {
            when(moreTimeRequestRule.validateMoreTimeCanBeRequested(any(Claim.class)))
                .thenReturn(List.of("a", "b", "c"));

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) moreTimeRequestedCallbackHandler.handle(callbackParams);

            assertThat(response.getErrors()).containsExactly("a", "b", "c");
        }

        @Test
        void shouldProvideResponseDeadline() {
            when(moreTimeRequestRule.validateMoreTimeCanBeRequested(any(Claim.class)))
                .thenReturn(Lists.emptyList());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) moreTimeRequestedCallbackHandler.handle(callbackParams);

            assertThat(response.getData()).hasSize(1)
                .containsEntry(CALCULATED_RESPONSE_DEADLINE, CCDCalculatedResponseDeadline.builder()
                    .calculatedResponseDeadline(Formatting.formatDate(deadline)).build());
        }
    }
    @Nested
    @DisplayName("Mid callback test")
    class MidCallbackTest {
        @BeforeEach
        void setUp() {
            callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();
            ccdCase =ccdCase.toBuilder()
                .calculatedResponseDeadlineContent(CCDCalculatedResponseDeadline.builder()
                    .calculatedResponseDeadline(Formatting.formatDate(deadline)).build())
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        }

        @Test
        void shouldGenerateLetter() {
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) moreTimeRequestedCallbackHandler.handle(callbackParams);

            assertThat(response.getData()).hasSize(1)
                .containsEntry(GeneralLetterService.DRAFT_LETTER_DOC, CCDDocument.builder()
                    .documentUrl(DOC_URL).build());
        }
    }

    @Nested
    @DisplayName("Event Producer Test")
    class EventProducerTest {
        @BeforeEach
        void setUp() {
            claim = claim.toBuilder().responseDeadline(deadline).build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()))
                .thenReturn(deadline);
        }

        @Test
        void shouldGenerateEventOnAboutToSubmit() {
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);

            verify(eventProducer).createMoreTimeForResponseRequestedEvent(
                eq(claim),
                eq(deadline),
                eq(claim.getClaimData().getDefendant().getEmail().get())
            );
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("Email sent Tests")
    class EmailNotificationSent {
        @BeforeEach
        void setUp() {
            claim = claim.toBuilder()
                .referenceNumber("reference")
                .issuedOn(LocalDate.now())
                .responseDeadline(LocalDate.now().plusDays(28))
                .claimData(SampleClaimData.submittedByClaimant())
                .defendantEmail(DEFENDANT_EMAIL).defendantId(DEFENDANT_ID).build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()))
                .thenReturn(deadline);
        }

        @Test
        void shouldSendEmailToLinkedDefendant() {
            when(emailTemplates.getDefendantMoreTimeRequested()).thenReturn(DEFENDANT_TEMPLATE_ID);
            moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
            verify(notificationService, once()).sendMail(
                eq(claim.getDefendantEmail()),
                eq(DEFENDANT_TEMPLATE_ID),
                anyMap(),
                eq(SampleMoreTimeRequestedEvent.getReference("defendant", claim.getReferenceNumber()))
            );
        }

        @Test
        void sendEmailToClaimant() {
            when(emailTemplates.getClaimantMoreTimeRequested()).thenReturn(CLAIMANT_TEMPLATE_ID);
            moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
            verify(notificationService, once()).sendMail(
                eq(claim.getSubmitterEmail()),
                eq(CLAIMANT_TEMPLATE_ID),
                anyMap(),
                eq(SampleMoreTimeRequestedEvent.getReference("claimant", claim.getReferenceNumber()))
            );
        }
    }

    @Nested
    @DisplayName("Letter sent Tests")
    class LetterNotificationSent {
        @BeforeEach
        void setUp() {
            claim = claim.toBuilder()
                .referenceNumber("reference")
                .issuedOn(LocalDate.now())
                .responseDeadline(LocalDate.now().plusDays(28))
                .claimData(SampleClaimData.submittedByClaimant())
                .defendantEmail(null).defendantId(null).build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()))
                .thenReturn(deadline);
        }

        @Test
        void sendLetterToNotLinkedDefendant() {
            when(generalLetterService.processDocuments(ccdCase, claim, AUTHORISATION, DOC_NAME)).thenReturn(ccdCase);

            moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);

            verify(generalLetterService)
                .processDocuments(any(CCDCase.class), any(Claim.class), anyString(), anyString());

        }

        @Test
        void shouldReturnWithErrorsWhenFailsToCreateDoc() {

            var response = (AboutToStartOrSubmitCallbackResponse)
                moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
            assertThat(response.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
        }
    }
}
