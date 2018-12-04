package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.RejectOrganisationPaymentPlanStaffNotificationService;

@Component
public class ClaimantResponseActionsHandler {

    private final NotificationToDefendantService notificationService;
    private final RejectOrganisationPaymentPlanStaffNotificationService rejectOrganisationPaymentPlanStaffNotificationService;

    public ClaimantResponseActionsHandler(
        NotificationToDefendantService notificationService,
        RejectOrganisationPaymentPlanStaffNotificationService rejectOrganisationPaymentPlanStaffNotificationService
    ) {
        this.notificationService = notificationService;
        this.rejectOrganisationPaymentPlanStaffNotificationService = rejectOrganisationPaymentPlanStaffNotificationService;
    }

    @EventListener
    public void sendNotificationToDefendant(ClaimantResponseEvent event) {
        this.notificationService.notifyDefendant(event.getClaim());
    }

    @EventListener
    public void sendClaimantRejectOrganisationPaymentPlanNotificationToStaff(RejectOrganisationPaymentPlanEvent event) {
        this.rejectOrganisationPaymentPlanStaffNotificationService.notifyStaffClaimantRejectOrganisationPaymentPlan(event.getClaim());
    }
}
