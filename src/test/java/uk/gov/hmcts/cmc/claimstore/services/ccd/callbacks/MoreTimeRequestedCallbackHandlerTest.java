package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getDefault;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithResponseDefendantEmailVerified;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestedCallbackHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";
    private static final String CLAIMANT_TEMPLATE_ID = "claimant template id";
    public static final String GENERAL_LETTER_TEMPLATE_ID = "generalLetterTemplateId";
    private Map<String, Object> data;
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
            .documentFileName(DOC_NAME)
            .documentBinaryUrl(DOC_URL_BINARY)
            .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
    private static final String ERROR_MESSAGE =
            "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";
    private static final CCDDocument DOCUMENT = CCDDocument
            .builder()
            .documentUrl(DOCUMENT_URL)
            .documentBinaryUrl(DOCUMENT_BINARY_URL)
            .documentFileName(DOCUMENT_FILE_NAME)
            .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .value(CCDClaimDocument.builder()
                            .documentLink(DOCUMENT)
                            .createdDatetime(DATE)
                            .documentName("general-letter-2020-01-01")
                            .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                            .build())
                    .build();

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
    private ApplicationEventPublisher publisher;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;

    private Claim claim;

    private CallbackRequest callbackRequest;

    private MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler;

    private CCDCase ccdCase;

    private CaseDetails caseDetails;

    private UserDetails userDetails;

    @Before
    public void setUp() {
        moreTimeRequestedCallbackHandler = new MoreTimeRequestedCallbackHandler (
                eventProducer,
                responseDeadlineCalculator,
                moreTimeRequestRule,
                caseDetailsConverter,
                notificationService,
                notificationsProperties,
                generalLetterService,
                userService,
                GENERAL_LETTER_TEMPLATE_ID
        );
        claim = SampleClaim.getDefault();
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        CCDRespondent ccdRespondent = SampleCCDDefendant.withDefault().build();
        ccdCase = CCDCase.builder()
                .previousServiceCaseReference("000MC001")
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
        caseDetails = CaseDetails.builder()
                .data(data)
                .build();
        userDetails = SampleUserDetails.builder()
                .withForename("Judge")
                .withSurname("McJudge")
                .build();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.RESPONSE_MORE_TIME.getValue())
                .caseDetails(CaseDetails
                    .builder()
                    .id(10L)
                    .data(Collections.emptyMap())
                    .build())
                .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantMoreTimeRequested()).thenReturn(DEFENDANT_TEMPLATE_ID);
        when(emailTemplates.getClaimantMoreTimeRequested()).thenReturn(CLAIMANT_TEMPLATE_ID);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn())).thenReturn(LocalDate.now());
        when(userDetails.getFullName()).thenReturn("Full name");
    }
    //PASSED
    @Test
    public void shouldValidateRequestOnAboutToStartEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();
        List<String> validationResults = ImmutableList.of("a", "b", "c");
        when(moreTimeRequestRule.validateMoreTimeCanBeRequested(claim))
            .thenReturn(validationResults);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getErrors()).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldCompleteRequestOnAboutToSubmitEvent() {

    }
    //PASSED
    @Test
    public void shouldGenerateEventOnAboutToSubmit() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        verify(eventProducer).createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().orElse(null)
        );

        assertThat(response).isNotNull();
    }


    @Test
    public void shouldSendEmailToLinkedDefendant() {
        CCDRespondent ccdRespondent = SampleCCDDefendant.withDefault().build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .data(caseDetailsConverter.convertToMap(
                                        SampleData.getCCDCitizenCaseWithRespondent(ccdRespondent)))
                                .build()
                        )
                .build();
        CallbackParams callbackParams = CallbackParams.builder().request(callbackRequest).build();
        moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
        verify(notificationService, once()).sendMail(
                eq(claim.getDefendantEmail()),
                eq(DEFENDANT_TEMPLATE_ID),
                anyMap(),
                eq(SampleMoreTimeRequestedEvent.getReference("defendant", claim.getReferenceNumber()))
        );
    }

    @Test
    public void sendEmailToClaimant() {
        CallbackParams callbackParams = CallbackParams.builder().request(callbackRequest).build();
        moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
        verify(notificationService, once()).sendMail(
                eq(claim.getSubmitterEmail()),
                eq(CLAIMANT_TEMPLATE_ID),
                anyMap(),
                eq(SampleMoreTimeRequestedEvent.getReference("claimant", claim.getReferenceNumber()))
        );
    }

    @Test
    public void sendLetterToNotLinkedDefendant() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .data(caseDetailsConverter.convertToMap(
                                        SampleData.getCCDCitizenCase(Collections.emptyList())))
                                .build()
                )
                .build();
        CallbackParams callbackParams = CallbackParams.builder().request(callbackRequest).build();
        moreTimeRequestedCallbackHandler.sendNotifications(callbackParams);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        doNothing().when(publisher).publishEvent(any(GeneralLetterReadyToPrintEvent.class));
        when(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
                .thenReturn(PDF_BYTES);
        verify(generalLetterService, once()).printAndUpdateCaseDocuments(eq(ccdCase), eq(claim), eq(BEARER_TOKEN.name()));

    }

    //PASSED
    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(callbackRequest)
            .build();
        moreTimeRequestedCallbackHandler
            .handle(callbackParams);
    }
}
