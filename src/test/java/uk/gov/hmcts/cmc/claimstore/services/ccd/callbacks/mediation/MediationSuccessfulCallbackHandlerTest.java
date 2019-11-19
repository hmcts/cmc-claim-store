package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MEDIATION_SUCCESSFUL;

@RunWith(MockitoJUnitRunner.class)
public class MediationSuccessfulCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationProperties;

    @Mock
    private MediationSuccessfulCallbackHandler mediationSuccessfulCallbackHandler;

    @Mock
    private MediationSuccessfulNotificationService mediationSuccessfulNotificationService;

    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";

    private Claim claimSetForMediation =
            SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        mediationSuccessfulCallbackHandler = new MediationSuccessfulCallbackHandler(
                caseDetailsConverter,
                notificationService,
                notificationProperties
                );
        callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(MEDIATION_SUCCESSFUL.getValue())
                .build();

        callbackParams = CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();
    }

    @Test
    public void shouldSendNotificationToParties() {
        Claim claim = claimSetForMediation.toBuilder()
                .response(
                        SampleResponse
                                .FullDefence
                                .builder()
                                .withDirectionsQuestionnaire(
                                        SampleDirectionsQuestionnaire.builder()
                                                .withHearingLocation(SampleHearingLocation.pilotHearingLocation)
                                                .build()
                                ).build()
                )
                .features(Collections.singletonList("directionsQuestionnaire"))
                .build();

        when(caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails())).thenReturn(claim);

        mediationSuccessfulCallbackHandler.handle(callbackParams);

        verify(mediationSuccessfulNotificationService).notifyParties(any());
    }
}
