package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.TheirDetailsHelper.isDefendantBusiness;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_TRANSFER;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.isPilotCourt;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isJudgePilot;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isLegalAdvisorPilot;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isOnlineDQ;

public class DirectionsQuestionnaireUtils {

    private DirectionsQuestionnaireUtils() {
        // utility class, no instances
    }

    public static Optional<CaseEvent> prepareCaseEvent(ResponseRejection responseRejection, Claim claim) {
        if (isOptedForMediation(responseRejection)) {
            return Optional.of(REFERRED_TO_MEDIATION);
        } else if (isOnlineDQ(claim)) {
            if (isPilotCourt(getPreferredCourt(claim))) {
                if (isLegalAdvisorPilot(claim)) {
                    return Optional.of(ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS);
                }
                if (isJudgePilot(claim)) {
                    return Optional.of(ASSIGNING_FOR_JUDGE_DIRECTIONS);
                }
            }

            return Optional.of(WAITING_TRANSFER);
        } else {
            return Optional.empty();
        }
    }

    public static String getDirectionsCaseState(Claim claim) {
        if (isPilotCourt(getPreferredCourt(claim))) {
            if (isLegalAdvisorPilot(claim)) {
                return READY_FOR_LEGAL_ADVISOR_DIRECTIONS.getValue();
            }
            if (isJudgePilot(claim)) {
                return READY_FOR_JUDGE_DIRECTIONS.getValue();
            }
        }

        return READY_FOR_TRANSFER.getValue();
    }

    public static String getPreferredCourt(Claim claim) {
        if (!FeaturesUtils.isOnlineDQ(claim)) {
            return null;
        }

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
