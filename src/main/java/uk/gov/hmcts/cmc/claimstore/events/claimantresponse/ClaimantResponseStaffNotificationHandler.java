package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

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
        if (isResponseFullDefenceStatesPaid(claim)) {
            this.statesPaidStaffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claim);
        }
    }

    private static boolean isResponseFullDefenceStatesPaid(Claim claim) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalStateException::new).getResponseType();

        if (responseType == ResponseType.FULL_DEFENCE) {
            FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) claim.getResponse()
                .orElseThrow(IllegalStateException::new);
            return fullDefenceResponse.getDefenceType() == DefenceType.ALREADY_PAID;
        }
        return false;
    }
}
