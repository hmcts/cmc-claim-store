package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseFullDefenceStatesPaid;

@Component
public class ClaimantResponseStaffNotificationHandler {

    private final StatesPaidStaffNotificationService statesPaidStaffNotificationService;

    @Autowired
    public ClaimantResponseStaffNotificationHandler(
        StatesPaidStaffNotificationService statesPaidStaffNotificationService) {
        this.statesPaidStaffNotificationService = statesPaidStaffNotificationService;
    }

    @EventListener
    public void onClaimantResponse(ClaimantResponseEvent event) {
        Claim claim = event.getClaim();
        if (isResponseFullDefenceStatesPaid(claim.getResponse().orElseThrow(IllegalArgumentException::new))) {
            this.statesPaidStaffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claim);
        }
    }
}
