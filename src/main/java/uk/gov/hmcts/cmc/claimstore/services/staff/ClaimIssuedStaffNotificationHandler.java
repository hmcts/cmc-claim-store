package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;

import static java.util.Objects.requireNonNull;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class ClaimIssuedStaffNotificationHandler {

    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public ClaimIssuedStaffNotificationHandler(
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService
    ) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    @EventListener
    public void notifyStaff(DocumentGeneratedEvent event) {
        requireNonNull(event);

        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(event.getClaim(), event.getDocuments());
    }
}
