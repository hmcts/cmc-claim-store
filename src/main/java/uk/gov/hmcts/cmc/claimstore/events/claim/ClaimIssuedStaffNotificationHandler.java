package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "false")
public class ClaimIssuedStaffNotificationHandler implements StaffNotificationHandler {

    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public ClaimIssuedStaffNotificationHandler(
        final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService
    ) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    @EventListener
    public void onClaimIssued(final ClaimIssuedEvent event) {
        claimIssuedStaffNotificationService.notifyStaffClaimIssued(
            event.getClaim(),
            event.getPin().orElse(null),
            event.getSubmitterEmail(),
            event.getAuthorisation()
        );
    }

    @EventListener
    public void onRepresentedClaimIssued(final RepresentedClaimIssuedEvent event) {
        claimIssuedStaffNotificationService.notifyStaffClaimIssued(
            event.getClaim(),
            null,
            event.getRepresentativeEmail(),
            event.getAuthorisation()
        );
    }
}
