package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.CourtLocationType;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediationUnsuccessfulCallbackHandlerTest {

    private static final String TRANSFER_CLAIMANT = "TRANSFER_CLAIMANT";
    private static final String TRANSFER_DEFENDANT = "TRANSFER_DEFENDANT";
    private static final String DIRECTIONS_CLAIMANT = "DIRECTIONS_CLAIMANT";
    private static final String DIRECTIONS_DEFENDANT = "DIRECTIONS_DEFENDANT";
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private NotificationTemplates notificationTemplates;

    private MediationUnsuccessfulCallbackHandler mediationUnsuccessfulCallbackHandler;

    private Claim claim;

    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        mediationUnsuccessfulCallbackHandler = new MediationUnsuccessfulCallbackHandler(
            caseDetailsConverter,
            notificationService,
            notificationsProperties
        );

        callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL.getValue())
            .caseDetails(
                CaseDetails.builder()
                    .id(10L)
                    .data(Collections.emptyMap())
                    .build())
            .build();


        when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
        when(notificationTemplates.getEmail()).thenReturn(emailTemplates);

        when(emailTemplates.getClaimantReadyForDirections())
            .thenReturn(DIRECTIONS_CLAIMANT);
        when(emailTemplates.getDefendantReadyForDirections())
            .thenReturn(DIRECTIONS_DEFENDANT);
        when(emailTemplates.getClaimantReadyForTransfer())
            .thenReturn(TRANSFER_CLAIMANT);
        when(emailTemplates.getDefendantReadyForTransfer())
            .thenReturn(TRANSFER_DEFENDANT);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn("BASELINE_URL");
    }


    @Test
    public void shouldSendTransferNotificationsWhenNonPilotCourtIsSelected() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .build();

        claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .build();

        when(caseDetailsConverter.extractClaim(any())).thenReturn(claim);

        SubmittedCallbackResponse response =
            (SubmittedCallbackResponse)mediationUnsuccessfulCallbackHandler
                .handle(callbackParams);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()), eq(TRANSFER_CLAIMANT), any(), any());
        verify(notificationService).sendMail(eq(claim.getDefendantEmail()), eq(TRANSFER_DEFENDANT), any(), any());
        assertThat(response).isNotNull();
    }

    @Test
    public void shouldSendDirectionsNotificationsWhenPilotCourtIsSelected() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .build();

        claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.builder().withDirectionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName("Birmingham")
                        .hearingLocationSlug("a-court")
                        .courtAddress(SampleAddress.builder().build())
                        .locationOption(CourtLocationType.ALTERNATE_COURT)
                        .build())
                    .build())
                .build())
            .withDefendantEmail("defendant@mail.com")
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .build();

        when(caseDetailsConverter.extractClaim(any())).thenReturn(claim);

        SubmittedCallbackResponse response =
            (SubmittedCallbackResponse)mediationUnsuccessfulCallbackHandler
                .handle(callbackParams);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()), eq(DIRECTIONS_CLAIMANT), any(), any());
        verify(notificationService).sendMail(eq(claim.getDefendantEmail()), eq(DIRECTIONS_DEFENDANT), any(), any());
        assertThat(response).isNotNull();
    }

}

