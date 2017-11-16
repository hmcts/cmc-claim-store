package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;

import java.util.Optional;

@Component
public class ClaimIssuedStaffNotificationHandler {

    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public ClaimIssuedStaffNotificationHandler(
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    @EventListener
    public void onClaimIssued(final ClaimIssuedEvent event) {
        claimIssuedStaffNotificationService.notifyStaffClaimIssued(
            event.getClaim(),
            Optional.of(event.getPin()),
            event.getSubmitterEmail()
        );
    }

    @EventListener
    public void onRepresentedClaimIssued(final RepresentedClaimIssuedEvent event) {
        claimIssuedStaffNotificationService.notifyStaffClaimIssued(
            event.getClaim(),
            Optional.empty(),
            event.getRepresentativeEmail()
        );
    }
}
