package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.TheirDetailsHelper.isDefendantBusiness;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.isPilotCourt;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;

public class DirectionsQuestionnaireUtils {

    public static final String DQ_FLAG = "directionsQuestionnaire";
    public static final String LA_PILOT_FLAG = "LAPilotEligible";

    private DirectionsQuestionnaireUtils() {
        // utility class, no instances
    }

    public static boolean isOnlineDQ(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(DQ_FLAG);
    }

    private static boolean isLegalAdvisorPilot(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(LA_PILOT_FLAG);
    }

    public static Optional<CaseEvent> prepareCaseEvent(ResponseRejection responseRejection, Claim claim) {
        if (isOptedForMediation(responseRejection)) {
            return Optional.of(REFERRED_TO_MEDIATION);
        }
        String preferredCourt = getPreferredCourt(claim);

        if (isLegalAdvisorPilot(claim) && isPilotCourt(preferredCourt)) {
            return Optional.of(ASSIGNING_FOR_DIRECTIONS);
        }

        if (isOnlineDQ(claim)) {
            return Optional.of(WAITING_TRANSFER);
        }

        return Optional.empty();
    }

    public static String getPreferredCourt(Claim claim) {
        if (isDefendantBusiness(claim.getClaimData().getDefendant())) {
            ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
            return getClaimantHearingCourt(claimantResponse);
        } else {
            Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
            return getDefendantHearingCourt(defendantResponse);
        }
    }

    private static String getDefendantHearingCourt(Response defendantResponse) {
        if (defendantResponse.getResponseType() == FULL_DEFENCE) {
            return ((FullDefenceResponse) defendantResponse).getDirectionsQuestionnaire()
                .flatMap(DirectionsQuestionnaire::getHearingLocation)
                .map(HearingLocation::getCourtName)
                .orElseThrow(IllegalStateException::new);
        } else if (defendantResponse.getResponseType() == PART_ADMISSION) {
            return ((PartAdmissionResponse) defendantResponse).getDirectionsQuestionnaire()
                .flatMap(DirectionsQuestionnaire::getHearingLocation)
                .map(HearingLocation::getCourtName)
                .orElseThrow(IllegalStateException::new);
        } else {
            throw new IllegalStateException("No preferred court as defendant response is full admission");
        }
    }

    private static String getClaimantHearingCourt(ClaimantResponse claimantResponse) {
        if (claimantResponse.getType() == REJECTION) {
            return ((ResponseRejection) claimantResponse).getDirectionsQuestionnaire()
                .flatMap(DirectionsQuestionnaire::getHearingLocation)
                .map(HearingLocation::getCourtName)
                .orElseThrow(IllegalStateException::new);
        } else {
            throw new IllegalStateException("No preferred court as claimant response is not rejection.");
        }
    }
}
