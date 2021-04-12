package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.breathingspace;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpaceType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceEmailService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLiftedCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;

@ExtendWith(MockitoExtension.class)
@DisplayName("Breathing Space Entered Callback handler")
class BreathingSpaceLiftedCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String breathingSpaceLiftedTemplateID = "CV-CMC-LET-ENG-00636.docx";
    private static final String EMAIL_TO_CLAIMANT = "breathing space email to claimant";
    private static final String EMAIL_TO_DEFENDANT = "breathing space email to defendant";

    private BreathingSpaceLiftedCallbackHandler handler;

    private CallbackParams callbackParams;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private BreathingSpaceLetterService breathingSpaceLetterService;
    @Mock
    private BreathingSpaceEmailService breathingSpaceEmailService;
    @Mock
    private UserService userService;

    private Map<String, Object> returnMap = new HashMap<String, Object>();

    private CCDCase getCCDCase(CCDRespondent.CCDRespondentBuilder builder) {
        CCDBreathingSpace breathingSpace = new CCDBreathingSpace();
        breathingSpace.setBsReferenceNumber("REF12121212");
        breathingSpace.setBsType(CCDBreathingSpaceType.STANDARD_BS_ENTERED);
        breathingSpace.setBsEnteredDate(LocalDate.now());
        breathingSpace.setBsEnteredDateByInsolvencyTeam(LocalDate.now());
        breathingSpace.setBsExpectedEndDate(LocalDate.now());
        breathingSpace.setBsLiftedFlag("NO");

        return CCDCase.builder()
            .breathingSpace(breathingSpace)
            .previousServiceCaseReference("CMC")
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseWithValidationErrors(CCDRespondent.CCDRespondentBuilder builder) {
        CCDBreathingSpace breathingSpace = new CCDBreathingSpace();
        breathingSpace.setBsReferenceNumber("REF12121212232323232323232");
        breathingSpace.setBsType(CCDBreathingSpaceType.STANDARD_BS_ENTERED);
        breathingSpace.setBsEnteredDate(LocalDate.now());
        breathingSpace.setBsEnteredDateByInsolvencyTeam(LocalDate.now());
        breathingSpace.setBsExpectedEndDate(LocalDate.now());
        breathingSpace.setBsLiftedFlag("NO");

        return CCDCase.builder()
            .breathingSpace(breathingSpace)
            .previousServiceCaseReference("CMC")
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseWithInvalidInsolvencyStartDate(CCDRespondent.CCDRespondentBuilder builder) {
        CCDBreathingSpace breathingSpace = new CCDBreathingSpace();
        breathingSpace.setBsReferenceNumber("REF12121");
        breathingSpace.setBsType(CCDBreathingSpaceType.STANDARD_BS_ENTERED);
        breathingSpace.setBsEnteredDate(LocalDate.now());
        breathingSpace.setBsEnteredDateByInsolvencyTeam(LocalDate.now().plusDays(3));
        breathingSpace.setBsExpectedEndDate(LocalDate.now());
        breathingSpace.setBsLiftedFlag("NO");

        return CCDCase.builder()
            .breathingSpace(breathingSpace)
            .previousServiceCaseReference("CMC")
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseWithExpectedEndDateLesserThanTodaysDate(CCDRespondent.CCDRespondentBuilder builder) {
        CCDBreathingSpace breathingSpace = new CCDBreathingSpace();
        breathingSpace.setBsReferenceNumber("REF1212121");
        breathingSpace.setBsType(CCDBreathingSpaceType.STANDARD_BS_ENTERED);
        breathingSpace.setBsEnteredDate(LocalDate.now());
        breathingSpace.setBsEnteredDateByInsolvencyTeam(LocalDate.now());
        breathingSpace.setBsExpectedEndDate(LocalDate.now().minusDays(3));
        breathingSpace.setBsLiftedFlag("NO");

        return CCDCase.builder()
            .breathingSpace(breathingSpace)
            .previousServiceCaseReference("CMC")
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseWithoutBreathingSpace(CCDRespondent.CCDRespondentBuilder builder, ClaimState claimState) {

        return CCDCase.builder()
            .previousServiceCaseReference("CMC")
            .state(claimState.getValue())
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .build();
    }

    @Nested
    class AboutToStartTests {
        @BeforeEach
        void setUp() {
            handler = new BreathingSpaceLiftedCallbackHandler(caseDetailsConverter,
                notificationsProperties, breathingSpaceLiftedTemplateID, eventProducer, userService,
                breathingSpaceLetterService,
                breathingSpaceEmailService);
            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest);

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
        }

        private CallbackRequest getCallBackRequest() {
            return CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(EMPTY_MAP).build())
                .eventId(CaseEvent.BREATHING_SPACE_LIFTED.getValue())
                .build();
        }

        private CallbackParams getBuild(CallbackRequest callbackRequest) {
            return CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_START)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();
        }

        @Test
        void shouldGenerateEventOnAboutToStart() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(), ClaimState.OPEN);
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnAboutToStartForTransferredCases() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(), ClaimState.TRANSFERRED);
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .state(ClaimState.TRANSFERRED)
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnAboutToStartForBusinessQueueCases() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(), ClaimState.BUSINESS_QUEUE);
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .state(ClaimState.TRANSFERRED)
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnAboutToStartForHwfApplicationPendingCases() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(),
                ClaimState.HWF_APPLICATION_PENDING);
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .state(ClaimState.TRANSFERRED)
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnAboutToStartForAwaitingResponseHwfCases() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(),
                ClaimState.AWAITING_RESPONSE_HWF);
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .state(ClaimState.TRANSFERRED)
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnAboutToStartForClosedHwfCases() {
            CCDCase ccdCase = getCCDCaseWithoutBreathingSpace(CCDRespondent.builder(), ClaimState.CLOSED_HWF);
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .state(ClaimState.TRANSFERRED)
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }
    }

    @Nested
    class AboutToSubmitTests {
        @BeforeEach
        void setUp() {
            handler = new BreathingSpaceLiftedCallbackHandler(caseDetailsConverter,
                notificationsProperties, breathingSpaceLiftedTemplateID, eventProducer, userService,
                breathingSpaceLetterService,
                breathingSpaceEmailService);
            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest);
            CCDCase ccdCase = getCCDCase(CCDRespondent.builder());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            UserDetails userDetails = SampleUserDetails.builder()
                .withForename("Forename")
                .withSurname("Surname")
                .withRoles("caseworker-cmc")
                .build();
            when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        }

        private CallbackRequest getCallBackRequest() {
            return CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(EMPTY_MAP).build())
                .eventId(CaseEvent.BREATHING_SPACE_LIFTED.getValue())
                .build();
        }

        private CallbackParams getBuild(CallbackRequest callbackRequest) {
            return CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();
        }

        @Test
        void shouldNotSendLetterAndEmailsOnAboutToSubmit() {
            UserDetails userDetails = SampleUserDetails.builder()
                .withForename("Forename")
                .withSurname("Surname")
                .withRoles("citizen")
                .build();
            when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
            handler.handle(callbackParams);
            verify(breathingSpaceEmailService, times(0)).sendNotificationToClaimant(
                any(Claim.class),
                any(String.class)
            );
            verify(breathingSpaceEmailService, times(0)).sendEmailNotificationToDefendant(
                any(Claim.class),
                any(String.class)
            );
            verify(breathingSpaceLetterService, times(0)).sendLetterToDefendantFomCCD(any(CCDCase.class),
                any(Claim.class),
                any(String.class), any(String.class), anyString());
        }

        @Test
        void shouldGenerateEventOnAboutToSubmit() {
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .defendantId("id")
                .build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceLiftedEmailToClaimant()).thenReturn(EMAIL_TO_CLAIMANT);
            when(emailTemplates.getBreathingSpaceLiftedEmailToDefendant()).thenReturn(EMAIL_TO_DEFENDANT);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(callbackResponse).isNotNull();
        }

        @Test
        void notifyClaimant() {

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceLiftedEmailToClaimant()).thenReturn(EMAIL_TO_CLAIMANT);
            handler.handle(callbackParams);

            verify(breathingSpaceEmailService, times(1)).sendNotificationToClaimant(
                any(Claim.class),
                any(String.class)
            );
        }

        @Test
        void shouldNotifyDefendantByEmail() {

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .defendantId("id")
                .build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceLiftedEmailToDefendant()).thenReturn(EMAIL_TO_DEFENDANT);
            handler.handle(callbackParams);

            verify(breathingSpaceEmailService, times(1)).sendEmailNotificationToDefendant(
                any(Claim.class),
                any(String.class)
            );
        }

        @Test
        void shouldGenerateAndPublishDocument() {

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceLiftedEmailToClaimant()).thenReturn(EMAIL_TO_CLAIMANT);

            ArgumentCaptor<CCDCase> ccdDataArgumentCaptor = ArgumentCaptor.forClass(CCDCase.class);
            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdDataArgumentCaptor.capture());

            verify(breathingSpaceLetterService, times(1)).sendLetterToDefendantFomCCD(any(CCDCase.class),
                any(Claim.class),
                any(String.class), any(String.class), anyString());
        }

        @Test
        void shouldThrowExceptionWhenGenerateAndUpdateCaseDocumentFails() {

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            Assertions.assertThrows(RuntimeException.class,
                () -> handler.handle(callbackParams));
        }
    }

    @Nested
    class MidTests {
        @BeforeEach
        void setUp() {
            handler = new BreathingSpaceLiftedCallbackHandler(caseDetailsConverter,
                notificationsProperties, breathingSpaceLiftedTemplateID, eventProducer, userService,
                breathingSpaceLetterService,
                breathingSpaceEmailService);
            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest);

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();

        }

        private CallbackRequest getCallBackRequest() {
            return CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(EMPTY_MAP).build())
                .eventId(CaseEvent.BREATHING_SPACE_LIFTED.getValue())
                .build();
        }

        private CallbackParams getBuild(CallbackRequest callbackRequest) {
            return CallbackParams.builder()
                .type(CallbackType.MID)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();
        }

        @Test
        void shouldGenerateEventOnMid() {
            CCDCase ccdCase = getCCDCase(CCDRespondent.builder());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnMidValidationError() {
            CCDCase ccdCase = getCCDCaseWithValidationErrors(CCDRespondent.builder());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnMidWithCCDCaseWithInvalidInsolvencyStartDate() {
            CCDCase ccdCase = getCCDCaseWithInvalidInsolvencyStartDate(CCDRespondent.builder());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }

        @Test
        void shouldGenerateEventOnMidVWithCCDCaseWithExpectedEndDateLesserThanTodaysDate() {
            CCDCase ccdCase = getCCDCaseWithExpectedEndDateLesserThanTodaysDate(CCDRespondent.builder());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            CallbackResponse callbackResponse = handler.handle(callbackParams);
            assertThat(ccdCase).isNotNull();
        }
    }

    @Nested
    class SubmittedTests {
        @BeforeEach
        void setUp() {
            handler = new BreathingSpaceLiftedCallbackHandler(caseDetailsConverter,
                notificationsProperties, breathingSpaceLiftedTemplateID, eventProducer, userService,
                breathingSpaceLetterService,
                breathingSpaceEmailService);
            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceLiftedEmailToClaimant()).thenReturn(EMAIL_TO_CLAIMANT);
            when(emailTemplates.getBreathingSpaceLiftedEmailToDefendant()).thenReturn(EMAIL_TO_DEFENDANT);
            CCDCase ccdCase = getCCDCase(CCDRespondent.builder());
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            returnMap.put("BS", ccdCase);
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        }

        private CallbackRequest getCallBackRequest() {
            return CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(EMPTY_MAP).build())
                .eventId(CaseEvent.BREATHING_SPACE_LIFTED.getValue())
                .build();
        }

        private CallbackParams getBuild(CallbackRequest callbackRequest) {
            return CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();
        }

        @Test
        void shouldGenerateEventOnSubmittedForCitizen() {
            UserDetails userDetails = SampleUserDetails.builder()
                .withForename("Forename")
                .withSurname("Surname")
                .withRoles("citizen")
                .build();
            when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
            handler.handle(callbackParams);
            verify(eventProducer, times(1)).createBreathingSpaceLiftedEvent(
                any(Claim.class), any(CCDCase.class),
                anyString(), anyString(),
                anyString(), anyString(), any(Boolean.class), any(Boolean.class)
            );
        }

        @Test
        void shouldGenerateEventOnSubmittedForCaseWorker() {
            UserDetails userDetails = SampleUserDetails.builder()
                .withForename("Forename")
                .withSurname("Surname")
                .withRoles("caseworker-cmc")
                .build();
            when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
            handler.handle(callbackParams);
            verify(eventProducer, times(1)).createBreathingSpaceLiftedEvent(
                any(Claim.class), any(CCDCase.class),
                anyString(), anyString(),
                anyString(), anyString(), any(Boolean.class), any(Boolean.class)
            );
        }
    }
}
