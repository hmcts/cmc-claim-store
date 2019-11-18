package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseActionsHandlerTest {
    private final String authorisation = "Bearer authorisation";

    private ClaimantResponseActionsHandler handler;
    @Mock
    private NotificationToDefendantService notificationService;
    @Mock
    private ClaimantRejectOrgPaymentPlanStaffNotificationService claimantRejectOrgPaymentPlanStaffNotificationService;

    @Before
    public void setUp() {
        handler = new ClaimantResponseActionsHandler(
            notificationService,
            claimantRejectOrgPaymentPlanStaffNotificationService
        );
    }

    @Test
    public void sendNotificationToDefendantWhenFreeMediationConfirmed() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithFreeMediation();
        Response response = SampleResponse.FullDefence.builder().withMediation(YES).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfFreeMediationConfirmationByClaimant(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasSettledForFullDefense() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validDefaultAcceptation();
        Response response = SampleResponse.FullDefence.builder().withMediation(YES).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantSettling(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasIntentionToProceedForPaperDq() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.builder().build();
        Response response = SampleResponse.FullDefence.builder().withMediation(NO).build();
        Claim claim = SampleClaim.builder()
            .withResponse(response)
            .withClaimantResponse(claimantResponse)
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();

        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantIntentionToProceedForPaperDq(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasIntentionToProceedForOnlineDq() {
        //given
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .build();

        Response response = SampleResponse.FullDefence.builder()
            .withDirectionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .withMediation(NO)
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(response)
            .withClaimantResponse(claimantResponse)
            .withFeatures(ImmutableList.of(DQ_FLAG, "admissions"))
            .build();

        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantIntentionToProceedForOnlineDq(eq(claim));
    }

    @Test
    public void shouldNotSendNotificationToDefendantWhenFreeMediationNotConfirmed() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithFreeMediation();
        Response response = SampleResponse.FullDefence.builder().withMediation(NO).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService, never()).notifyDefendantOfFreeMediationConfirmationByClaimant(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantOfResponse() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validDefaultRejection();
        Response response = PartAdmission.builder().buildWithStatesPaid(SampleParty.builder().individual());
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantResponse(eq(claim));
    }
}
