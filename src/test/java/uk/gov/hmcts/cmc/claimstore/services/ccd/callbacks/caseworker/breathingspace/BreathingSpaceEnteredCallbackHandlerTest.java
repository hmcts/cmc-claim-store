package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.breathingspace;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceEnteredCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static java.util.Collections.EMPTY_MAP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;

@ExtendWith(MockitoExtension.class)
@DisplayName("Breathing Space Entered Callback handler")
class BreathingSpaceEnteredCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String breathingSpaceEnteredTemplateID = "CV-CMC-LET-ENG-00635.docx";
    private static final String EMAIL_TO_CLAIMANT = "breathing space email to claimant";
    private static final String EMAIL_TO_DEFENDANT = "breathing space email to defendant";

    private BreathingSpaceEnteredCallbackHandler handler;

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

    private CCDCase getCCDCase(CCDRespondent.CCDRespondentBuilder builder) {
        return CCDCase.builder()
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

    @Nested
    class AboutToSubmitTests {
        @BeforeEach
        void setUp() {
            handler = new BreathingSpaceEnteredCallbackHandler(caseDetailsConverter,
                notificationsProperties, breathingSpaceEnteredTemplateID, eventProducer);
            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest);
            CCDCase ccdCase = getCCDCase(CCDRespondent.builder());
            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(notificationsProperties.getTemplates()).thenReturn(templates);
            when(templates.getEmail()).thenReturn(emailTemplates);
            when(emailTemplates.getBreathingSpaceEmailToClaimant()).thenReturn(EMAIL_TO_CLAIMANT);
            when(emailTemplates.getBreathingSpaceEmailToDefendant()).thenReturn(EMAIL_TO_DEFENDANT);
        }

        private CallbackRequest getCallBackRequest() {
            return CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(EMPTY_MAP).build())
                .eventId(CaseEvent.BREATHING_SPACE_ENTERED.getValue())
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
        void shouldGenerateEventOnAboutToSubmit() {
            handler.handle(callbackParams);

            verify(eventProducer).createBreathingSpaceEnteredEvent(
                any(Claim.class), any(CCDCase.class),
                anyString(), anyString(),
                anyString(), anyString()
            );
        }
    }
}
