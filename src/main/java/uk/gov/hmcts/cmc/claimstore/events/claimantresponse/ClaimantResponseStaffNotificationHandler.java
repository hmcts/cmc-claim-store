package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isIntentToProceed;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isPartAdmission;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseFullDefenceStatesPaid;

@Component
public class ClaimantResponseStaffNotificationHandler {

    private final StatesPaidStaffNotificationService statesPaidStaffNotificationService;
    private final ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService;

    @Autowired
    public ClaimantResponseStaffNotificationHandler(
        StatesPaidStaffNotificationService statesPaidStaffNotificationService,
        ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService
    ) {
        this.statesPaidStaffNotificationService = statesPaidStaffNotificationService;
        this.claimantRejectionStaffNotificationService = claimantRejectionStaffNotificationService;
    }

    @EventListener
    public void onClaimantResponse(ClaimantResponseEvent event) {
        Claim claim = event.getClaim();
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        if (isResponseFullDefenceStatesPaid(response)) {
            this.statesPaidStaffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claim);
        }
        if (isPartAdmission(response)
            && claim.getClaimantResponse().orElseThrow(IllegalArgumentException::new).getType()
            == ClaimantResponseType.REJECTION) {
            claimantRejectionStaffNotificationService.notifyStaffClaimantRejectPartAdmission(claim);
        }
    }

    @EventListener
    public void notifyStaffWithClaimantsIntentionToProceed(ClaimantResponseEvent event) {
        ClaimantResponse claimantResponse = event.getClaim().getClaimantResponse()
            .orElseThrow(IllegalArgumentException::new);

        if (isIntentToProceed(claimantResponse)) {
            claimantRejectionStaffNotificationService.notifyStaffWithClaimantsIntentionToProceed(event.getClaim());
        }
    }
}
