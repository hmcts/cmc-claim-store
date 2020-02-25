package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
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
import static uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot.JDDO;
import static uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot.LA;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.TheirDetailsHelper.isDefendantBusiness;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_TRANSFER;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isJudgePilot;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isLegalAdvisorPilot;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.isOnlineDQ;

@Service
public class DirectionsQuestionnaireService {
    private final PilotCourtService pilotCourtService;

    public DirectionsQuestionnaireService(PilotCourtService pilotCourtService) {
        this.pilotCourtService = pilotCourtService;
    }

    public Optional<CaseEvent> prepareCaseEvent(ResponseRejection responseRejection, Claim claim) {
        if (isOptedForMediation(responseRejection)) {
            return Optional.of(REFERRED_TO_MEDIATION);
        }
        if (!isOnlineDQ(claim)) {
            return Optional.empty();
        }

        String preferredCourt = getPreferredCourt(claim);

        if (isLegalAdvisorPilot(claim) && pilotCourtService.isPilotCourt(preferredCourt, LA, claim.getCreatedAt())) {
            return Optional.of(ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS);
        }

        if (isJudgePilot(claim)  && pilotCourtService.isPilotCourt(preferredCourt, JDDO, claim.getCreatedAt())) {
            return Optional.of(ASSIGNING_FOR_JUDGE_DIRECTIONS);
        }

        return Optional.of(WAITING_TRANSFER);
    }

    public String getDirectionsCaseState(Claim claim) {

        String preferredCourt = getPreferredCourt(claim);

        if (isLegalAdvisorPilot(claim) && pilotCourtService.isPilotCourt(preferredCourt, LA, claim.getCreatedAt())) {
            return READY_FOR_LEGAL_ADVISOR_DIRECTIONS.getValue();
        }

        if (isJudgePilot(claim) && pilotCourtService.isPilotCourt(preferredCourt, JDDO, claim.getCreatedAt())) {
            return READY_FOR_JUDGE_DIRECTIONS.getValue();
        }

        return READY_FOR_TRANSFER.getValue();
    }

    public String getPreferredCourt(Claim claim) {
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

    private String getDefendantHearingCourt(Response defendantResponse) {
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
