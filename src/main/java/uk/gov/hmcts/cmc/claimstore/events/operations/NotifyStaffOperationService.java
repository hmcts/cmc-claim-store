package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class NotifyStaffOperationService {
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public NotifyStaffOperationService(ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    public Claim notify(Claim claim, String authorization, PDF... documents) {
        //TODO check claim if operation already complete, if yes return claim else

        DocumentGeneratedEvent documentGeneratedEvent = new DocumentGeneratedEvent(claim, authorization, documents);
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(documentGeneratedEvent);

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
