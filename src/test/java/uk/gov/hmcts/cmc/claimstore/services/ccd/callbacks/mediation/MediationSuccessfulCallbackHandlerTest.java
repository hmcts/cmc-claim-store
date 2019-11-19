package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class MediationSuccessfulCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private EmailTemplates emailTemplates;

    @Mock
    private MediationSuccessfulCallbackHandler mediationSuccessfulCallbackHandler;

    @Mock
    private NotificationTemplates notificationTemplates;

    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";

    private Claim claimSetForMediation =
            SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();

    private CallbackRequest callbackRequest;

    private static final String MEDIATION_SUCCESSFUL = "MEDIATION_SUCCESSFUL";

    @Before
    public void setUp() {

        mediationSuccessfulCallbackHandler = new MediationSuccessfulCallbackHandler(
                caseDetailsConverter,
                notificationService,
                notificationsProperties
                );
        when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);

        callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(MEDIATION_SUCCESSFUL)
                .build();

        callbackParams = CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();

        when(notificationTemplates.getEmail()).thenReturn(emailTemplates);

        when(emailTemplates.getClaimantMediationSuccess())
                .thenReturn(MEDIATION_SUCCESSFUL);
        when(emailTemplates.getDefendantMediationSuccess())
                .thenReturn(MEDIATION_SUCCESSFUL);

        when(notificationsProperties.getFrontendBaseUrl()).thenReturn("BASELINE_URL");
    }

    @Test
    public void shouldSendNotificationToClaimantWhithRightTemplate() {
        Claim claim = SampleClaim.builder()
                .withResponse(SampleResponse.FullDefence.validDefaults())
                .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
                .build().toBuilder().features(Arrays.asList("admissions", "directionsQuestionnaire")).build();

        mediationSuccessfulCallbackHandler.notifyClaimant(claim);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()),
                eq(MEDIATION_SUCCESSFUL),
                any(),
                eq("to-claimant-mediation-successful"));
    }

    @Test
    public void shouldSendNotificationToDefendantWhithRightTemplate() {
        Claim claim = SampleClaim.builder()
                .withResponse(SampleResponse.FullDefence.validDefaults())
                .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
                .build().toBuilder().features(Arrays.asList("admissions", "directionsQuestionnaire")).build();

        mediationSuccessfulCallbackHandler.notifyParties(callbackParams);

        verify(notificationService).sendMail(eq(claim.getDefendantEmail()),
                eq(MEDIATION_SUCCESSFUL),
                any(),
                eq("to-defendant-mediation-successful"));
    }

    @Test
    public void shouldSendNotificationToParties() {
        Claim claim = claimSetForMediation.toBuilder()
                .response(
                        SampleResponse
                                .FullDefence
                                .builder()
                                .withMediation(YesNoOption.YES).build()
                ).build();

        when(caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails())).thenReturn(claim);

        mediationSuccessfulCallbackHandler.handle(callbackParams);

        verify(mediationSuccessfulCallbackHandler).notifyParties(any());
    }
}
