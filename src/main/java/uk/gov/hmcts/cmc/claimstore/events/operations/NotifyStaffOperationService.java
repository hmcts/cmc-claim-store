package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Arrays;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class NotifyStaffOperationService {
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public NotifyStaffOperationService(ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    public Claim notify(Claim claim, String authorisation, PDF... documents) {
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(claim, Arrays.asList(documents));
        return claim;
    }
}
