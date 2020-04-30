package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
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
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class MoreTimeRequestedCallbackHandlerTest {

    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";
    private static final String CLAIMANT_TEMPLATE_ID = "claimant template id";
    private Map<String, Object> data;
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String FRONTEND_BASE_URL = "http://some.host.dot.com";

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
                generalLetterService
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
        data = new HashMap<>();
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        data.put(LETTER_CONTENT, "content");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.RESPONSE_MORE_TIME.getValue())
                .caseDetails(caseDetails)
                .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .request(callbackRequest)
            .build();
    }

    @Nested
    @DisplayName("About to Start Validation test")
    class ValidationTest {
        @BeforeEach
        void setUp() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()))
                    .thenReturn(deadline);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            List<String> validationResults = List.of("a", "b", "c");
            when(moreTimeRequestRule.validateMoreTimeCanBeRequested(any(Claim.class)))
                .thenReturn(validationResults);
        }

        @Test
        void shouldValidateRequestOnAboutToStartEvent() {

            AboutToStartOrSubmitCallbackResponse response = moreTimeRequestedCallbackHandler
                    .requestMoreTimeViaCaseworker(callbackParams);
            assertThat(response.getErrors()).containsExactly("a", "b", "c");
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
                moreTimeRequestedCallbackHandler
                    .sendNotifications(callbackParams);

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
        void sendLetterToNotLinkedDefendant() throws Exception {
            when(generalLetterService.createAndPrintLetter(ccdCase, claim, AUTHORISATION,
                  LETTER_CONTENT, DOC_NAME, CCDContactPartyType.DEFENDANT)).thenReturn(ccdCase);
            moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
            verify(generalLetterService).createAndPrintLetter(any(CCDCase.class), any(Claim.class), anyString(),
                    anyString(), anyString(), any());

        }

        @Test
        void shouldReturnWithErrorsWhenFailsToCreateDoc() {
            var response = (AboutToStartOrSubmitCallbackResponse)
                moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
            assertThat(response.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
        }
    }
}
