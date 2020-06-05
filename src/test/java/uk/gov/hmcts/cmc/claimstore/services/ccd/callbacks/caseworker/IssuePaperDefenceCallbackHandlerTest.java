package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
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
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperDefenceCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperResponseNotificationService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
public class IssuePaperDefenceCallbackHandlerTest {
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
    private static final LocalDate now = LocalDate.now();
    private static final LocalDate responseDeadline = now;
    private static final LocalDate serviceDate = now;
    private static final LocalDate issueDate = now;

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
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private DocumentPublishService documentPublishService;
    @Mock
    private IssuePaperResponseNotificationService issuePaperResponseNotificationService;

    private Claim claim;
    private CallbackRequest callbackRequest;
    private IssuePaperDefenceCallbackHandler issuePaperDefenceCallbackHandler;
    private CCDCase ccdCase;
    private CallbackParams callbackParams;


    @BeforeEach
    void setUp() {
        issuePaperDefenceCallbackHandler = new IssuePaperDefenceCallbackHandler(
            caseDetailsConverter,
            responseDeadlineCalculator,
            issueDateCalculator,
            issuePaperResponseNotificationService,
            documentPublishService
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
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
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
                .eventId(CaseEvent.ISSUE_PAPER_DEFENSE_FORMS.getValue())
                .caseDetails(caseDetails)
                .build();
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
                .serviceDate(LocalDate.now().plusDays(5))
                .claimData(SampleClaimData.submittedByClaimant())
                .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();

            ccdCase = ccdCase.toBuilder()
                .calculatedResponseDeadline(responseDeadline)
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        }

        @Test
        void sendEmailToClaimant() {
            when(emailTemplates.getDefendantAskedToRespondByPost()).thenReturn(CLAIMANT_TEMPLATE_ID);
            issuePaperDefenceCallbackHandler.handle(callbackParams);
            verify(notificationService, once()).sendMail(
                eq(claim.getSubmitterEmail()),
                eq(CLAIMANT_TEMPLATE_ID),
                anyMap(),
                eq(String.format("paper-response-forms-sent-%s-%s", "claimant", claim.getReferenceNumber()))
            );
        }
    }

    @Nested
    @DisplayName("Letter sent Tests")
    class LetterNotificationSent {
        @BeforeEach
        void setUp() {
            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();

            claim = claim.toBuilder()
                .referenceNumber("reference")
                .issuedOn(LocalDate.now())
                .responseDeadline(LocalDate.now().plusDays(28))
                .serviceDate(LocalDate.now().plusDays(5))
                .claimData(SampleClaimData.submittedByClaimant())
                .build();

            ccdCase = ccdCase.toBuilder()
                .calculatedResponseDeadline(responseDeadline)
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        }

        @Test
        void sendLetterToDefendant() {
            when(generalLetterService.publishLetter(ccdCase, claim, AUTHORISATION, DOC_NAME)).thenReturn(ccdCase);

            issuePaperDefenceCallbackHandler.handle(callbackParams);

            verify(generalLetterService)
                .publishLetter(any(CCDCase.class), any(Claim.class), anyString(), anyString());

        }

        @Test
        void shouldReturnWithErrorsWhenFailsToCreateDoc() {
            when(generalLetterService.publishLetter(ccdCase, claim, AUTHORISATION, DOC_NAME))
                .thenThrow(new RuntimeException("error occurred"));

            var response = (AboutToStartOrSubmitCallbackResponse)
                issuePaperDefenceCallbackHandler.handle(callbackParams);

            assertThat(response.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
        }
    }
}





