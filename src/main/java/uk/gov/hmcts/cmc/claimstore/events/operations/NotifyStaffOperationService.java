package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import java.util.Arrays;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PIN_GENERATION_OPERATIONS;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled", havingValue = "true")
public class NotifyStaffOperationService {
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public NotifyStaffOperationService(
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService,
        ClaimCreationEventsStatusService eventsStatusService
    ) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
        this.eventsStatusService = eventsStatusService;
    }

    public Claim notify(Claim claim, String authorisation, PDF... documents) {

        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(claim, Arrays.asList(documents));

        if (claim.getClaimData().isClaimantRepresented()) {
            ClaimSubmissionOperationIndicators indicators = claim.getClaimSubmissionOperationIndicators().toBuilder()
                .defendantNotification(null)
                .staffNotification(YES)
                .bulkPrint(YES)
                .build();

            return eventsStatusService.updateClaimOperationCompletion(authorisation, claim.getId(),
                indicators,
                PIN_GENERATION_OPERATIONS);
        } else {
            return eventsStatusService.updateClaimOperationCompletion(authorisation, claim, PIN_GENERATION_OPERATIONS);
        }
    }
}
