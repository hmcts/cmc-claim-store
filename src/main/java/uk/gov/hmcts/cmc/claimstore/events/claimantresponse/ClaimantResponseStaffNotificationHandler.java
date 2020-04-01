package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.InterlocutoryJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isIntentToProceed;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isReferredToJudge;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.domain.utils.PartyUtils.isCompanyOrOrganisation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isPartAdmission;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseFullDefenceStatesPaid;

@Component
public class ClaimantResponseStaffNotificationHandler {

    private final StatesPaidStaffNotificationService statesPaidStaffNotificationService;
    private final ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService;
    private final ClaimantRejectOrgPaymentPlanStaffNotificationService rejectOrgPaymentPlanStaffNotificationService;
    private final CCJStaffNotificationHandler ccjStaffNotificationHandler;

    @Autowired
    public ClaimantResponseStaffNotificationHandler(
        StatesPaidStaffNotificationService statesPaidStaffNotificationService,
        ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService,
        ClaimantRejectOrgPaymentPlanStaffNotificationService rejectOrgPaymentPlanStaffNotificationService,
        CCJStaffNotificationHandler ccjStaffNotificationHandler
    ) {
        this.statesPaidStaffNotificationService = statesPaidStaffNotificationService;
        this.claimantRejectionStaffNotificationService = claimantRejectionStaffNotificationService;
        this.rejectOrgPaymentPlanStaffNotificationService = rejectOrgPaymentPlanStaffNotificationService;
        this.ccjStaffNotificationHandler = ccjStaffNotificationHandler;
    }

    @EventListener
    public void onClaimantResponse(ClaimantResponseEvent event) {
        Claim claim = event.getClaim();
        Response response = claim.getResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_RESPONSE));
        if (isResponseFullDefenceStatesPaid(response)) {
            this.statesPaidStaffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claim);
        }
        ClaimantResponse claimantResponse = claim.getClaimantResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_CLAIMANT_RESPONSE));
        if (isPartAdmission(response) && claimantResponse.getType() == ClaimantResponseType.REJECTION) {
            claimantRejectionStaffNotificationService.notifyStaffClaimantRejectPartAdmission(claim);
        }
        if (isReferredToJudge(claimantResponse)) {
            if (isCompanyOrOrganisation(response.getDefendant())) {
                rejectOrgPaymentPlanStaffNotificationService.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);
            } else {
                ccjStaffNotificationHandler.onInterlocutoryJudgmentEvent(new InterlocutoryJudgmentEvent(claim));
            }
        }
    }

    @EventListener
    public void notifyStaffWithClaimantsIntentionToProceed(ClaimantResponseEvent event) {
        ClaimantResponse claimantResponse = event.getClaim().getClaimantResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_CLAIMANT_RESPONSE));

        if (isIntentToProceed(claimantResponse)) {
            claimantRejectionStaffNotificationService.notifyStaffWithClaimantsIntentionToProceed(event.getClaim());
        }
    }
}
