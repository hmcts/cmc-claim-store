package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.InterlocutoryJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaid;

@Component
public class ClaimantResponseActionsHandler {

    private final NotificationToDefendantService notificationService;
    private final ClaimantRejectOrgPaymentPlanStaffNotificationService
        claimantRejectOrgPaymentPlanStaffNotificationService;

    public ClaimantResponseActionsHandler(
        NotificationToDefendantService notificationService,
        ClaimantRejectOrgPaymentPlanStaffNotificationService claimantRejectOrgPaymentPlanStaffNotificationService
    ) {
        this.notificationService = notificationService;
        this.claimantRejectOrgPaymentPlanStaffNotificationService =
            claimantRejectOrgPaymentPlanStaffNotificationService;
    }

    @EventListener
    public void sendNotificationToDefendant(ClaimantResponseEvent event) {
        if (isFreeMediationConfirmed(event.getClaim())) {
            this.notificationService.notifyDefendantOfFreeMediationConfirmationByClaimant(event.getClaim());
        } else if (hasClaimantSettledForFullDefense(event.getClaim())) {
            this.notificationService.notifyDefendantOfClaimantSettling(event.getClaim());
        } else if (isRejectedStatesPaidOrPartAdmission(event.getClaim())) {
            this.notificationService.notifyDefendantOfClaimantResponse(event.getClaim());
        } else if (hasIntentionToProceedAndIsPaperDq(event.getClaim())) {
            this.notificationService.notifyDefendantOfClaimantIntentionToProceedForPaperDq(event.getClaim());
        } else if (hasIntentionToProceedAndIsOnlineDq(event.getClaim())) {
            this.notificationService.notifyDefendantOfClaimantIntentionToProceedForOnlineDq(event.getClaim());
        } else {
            this.notificationService.notifyDefendant(event.getClaim());
        }
    }

    private boolean hasIntentionToProceedAndIsOnlineDq(Claim claim) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        return ClaimantResponseHelper.isIntentToProceed(claimantResponse)
            && DirectionsQuestionnaireUtils.isOnlineDQ(claim);
    }

    private boolean hasIntentionToProceedAndIsPaperDq(Claim claim) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        return claimantResponse.getType() == REJECTION
            && !DirectionsQuestionnaireUtils.isOnlineDQ(claim);
    }

    private boolean isFreeMediationConfirmed(Claim claim) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        return claimantResponse.getType() == ClaimantResponseType.REJECTION
            && isOptedForMediation(claimantResponse)
            && isOptedForMediation(response);
    }

    private boolean hasClaimantSettledForFullDefense(Claim claim) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        return claimantResponse.getType() == ClaimantResponseType.ACCEPTATION
            && response.getResponseType() == ResponseType.FULL_DEFENCE;
    }

    @EventListener
    public void sendNotificationToDefendantWhenInterlocutoryJudgmentRequested(InterlocutoryJudgmentEvent event) {
        this.notificationService.notifyDefendantWhenInterlocutoryJudgementRequested(event.getClaim());
    }

    @EventListener
    public void sendClaimantRejectOrganisationPaymentPlanNotificationToStaff(RejectOrganisationPaymentPlanEvent event) {
        this.claimantRejectOrgPaymentPlanStaffNotificationService
            .notifyStaffClaimantRejectOrganisationPaymentPlan(event.getClaim());
    }

    private boolean isRejectedStatesPaidOrPartAdmission(Claim claim) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        return claimantResponse.getType() == ClaimantResponseType.REJECTION
            && (isResponseStatesPaid(response) || response.getResponseType() == ResponseType.PART_ADMISSION);
    }
}
