package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementReachedStaffNotificationService;

@Component
public class AgreementCountersignedStaffNotificationHandler {

    private final SettlementReachedStaffNotificationService notificationService;
    private final boolean staffEmailsEnabled;

    @Autowired
    public AgreementCountersignedStaffNotificationHandler(
        SettlementReachedStaffNotificationService notificationService,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled) {
        this.notificationService = notificationService;
        this.staffEmailsEnabled = staffEmailsEnabled;
    }

    @EventListener
    public void onAgreementCountersigned(AgreementCountersignedEvent event) {
        if (staffEmailsEnabled && event.getClaim().getClaimData().isClaimantRepresented()) {
            notificationService.notifySettlementReached(
                event.getClaim()
            );
        }
    }

}
