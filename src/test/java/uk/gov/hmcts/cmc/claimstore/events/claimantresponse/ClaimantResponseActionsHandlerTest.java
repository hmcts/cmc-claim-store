package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseActionsHandlerTest {
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
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfFreeMediationConfirmationByClaimant(eq(claim));
    }

    @Test
    public void shouldNotSendNotificationToDefendantWhenFreeMediationNotConfirmed() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithFreeMediation();
        Response response = SampleResponse.FullDefence.builder().withMediation(NO).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim);
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
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantResponse(eq(claim));
    }
}
