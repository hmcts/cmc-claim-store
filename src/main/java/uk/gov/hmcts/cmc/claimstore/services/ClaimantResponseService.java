package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LIFT_STAY;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isSettlePreJudgment;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;

@Service
public class ClaimantResponseService {

    private final ClaimService claimService;
    private final AppInsights appInsights;
    private final CaseRepository caseRepository;
    private final ClaimantResponseRule claimantResponseRule;
    private final EventProducer eventProducer;
    private final FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    @Value("${feature_toggles.directions_questionnaire_enabled:false}")
    boolean directionsQuestionnaireEnabled;

    @SuppressWarnings("squid:S00107") // All parameters are required here
    public ClaimantResponseService(
        ClaimService claimService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ClaimantResponseRule claimantResponseRule,
        EventProducer eventProducer,
        FormaliseResponseAcceptanceService formaliseResponseAcceptanceService,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator
    ) {
        this.claimService = claimService;
        this.appInsights = appInsights;
        this.caseRepository = caseRepository;
        this.claimantResponseRule = claimantResponseRule;
        this.eventProducer = eventProducer;
        this.formaliseResponseAcceptanceService = formaliseResponseAcceptanceService;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
    }

    public void save(
        String externalId,
        String claimantId,
        ClaimantResponse claimantResponse,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);
        claimantResponseRule.assertCanBeRequested(claim, claimantId);

        if (claim.getState().equals(ClaimState.STAYED) && isFullAdmissionOrPartAdmission(claim)) {
            claim = caseRepository.saveCaseEvent(authorization, claim, LIFT_STAY);
        }

        Claim updatedClaim = caseRepository.saveClaimantResponse(claim, claimantResponse, authorization);

        claimantResponseRule.isValid(updatedClaim);
        formaliseResponseAcceptance(claimantResponse, updatedClaim, authorization);

        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        if (isFullDefenseDisputeAcceptation(response, claimantResponse)) {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, updatedClaim.getReferenceNumber());

            caseRepository.saveCaseEvent(authorization, updatedClaim, CaseEvent.STAY_CLAIM);
        }

        if (!DirectionsQuestionnaireUtils.isOnlineDQ(updatedClaim)
            && isRejectResponseNoMediation(claimantResponse)) {
            updateDirectionsQuestionnaireDeadline(updatedClaim, authorization);
            updatedClaim = claimService.getClaimByExternalId(externalId, authorization);
        }

        if (!isSettlementAgreement(claim, claimantResponse)) {
            eventProducer.createClaimantResponseEvent(updatedClaim, authorization);
        }

        if (isSettlePreJudgment(claimantResponse)) {
            caseRepository.saveCaseEvent(authorization, updatedClaim, SETTLED_PRE_JUDGMENT);
        }

        if (directionsQuestionnaireEnabled && claimantResponse.getType() == REJECTION) {
            Optional<CaseEvent> caseEvent = DirectionsQuestionnaireUtils.prepareCaseEvent(
                (ResponseRejection) claimantResponse,
                updatedClaim
            );
            if (caseEvent.isPresent()) {
                caseRepository.saveCaseEvent(authorization, updatedClaim, caseEvent.get());
            }
        }

        AppInsightsEvent appInsightsEvent = getAppInsightsEvent(updatedClaim, claimantResponse);
        appInsights.trackEvent(appInsightsEvent, "referenceNumber", claim.getReferenceNumber());

        if (isRejectResponseWithMediation(claim, claimantResponse)) {
            if (claim.getFeatures() != null && claim.getFeatures().contains("mediationPilot")) {
                appInsights.trackEvent(AppInsightsEvent.MEDIATION_PILOT_ELIGIBLE,
                    "referenceNumber", claim.getReferenceNumber());
            } else {
                appInsights.trackEvent(AppInsightsEvent.MEDIATION_NON_PILOT_ELIGIBLE,
                    "referenceNumber", claim.getReferenceNumber());
            }
        }
    }

    private boolean isSettlementAgreement(Claim claim, ClaimantResponse claimantResponse) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        if (shouldFormaliseResponseAcceptance(response, claimantResponse)) {
            return ((ResponseAcceptation) claimantResponse).getFormaliseOption()
                .filter(Predicate.isEqual(FormaliseOption.SETTLEMENT))
                .isPresent();
        }
        return false;
    }

    private boolean isRejectResponseNoMediation(ClaimantResponse claimantResponse) {
        return ClaimantResponseType.REJECTION.equals(claimantResponse.getType())
            && ((ResponseRejection) claimantResponse).getFreeMediation()
            .filter(Predicate.isEqual(YesNoOption.NO))
            .isPresent();
    }

    private void updateDirectionsQuestionnaireDeadline(Claim claim, String authorization) {
        LocalDate deadline = directionsQuestionnaireDeadlineCalculator
            .calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime.now());
        caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
    }

    private void formaliseResponseAcceptance(ClaimantResponse claimantResponse, Claim claim, String authorization) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        if (shouldFormaliseResponseAcceptance(response, claimantResponse)) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
            if (responseAcceptation.getFormaliseOption().isPresent()) {
                formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, authorization);
            }
        }
    }

    private AppInsightsEvent getAppInsightsEvent(Claim claim, ClaimantResponse claimantResponse) {
        if (claimantResponse instanceof ResponseAcceptation) {
            return AppInsightsEvent.CLAIMANT_RESPONSE_ACCEPTED;
        } else if (claimantResponse instanceof ResponseRejection) {
            return getEventNameForRejection(claim);
        } else {
            throw new IllegalStateException("Unknown response type");
        }
    }

    private AppInsightsEvent getEventNameForRejection(Claim claim) {
        return isPartAdmissionOrIsStatePaidOrIsFullDefence(claim) && DirectionsQuestionnaireUtils.isOnlineDQ(claim)
            ? AppInsightsEvent.LA_PILOT_ELIGIBLE
            : AppInsightsEvent.NON_LA_CASES;
    }

    private boolean isFullAdmissionOrPartAdmission(Claim claim) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        ResponseType responseType = response.getResponseType();

        return responseType == ResponseType.FULL_ADMISSION
            || responseType == ResponseType.PART_ADMISSION;
    }

    private boolean isPartAdmissionOrIsStatePaidOrIsFullDefence(Claim claim) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        ResponseType responseType = response.getResponseType();
        return responseType == ResponseType.PART_ADMISSION
            || isStatePaid(response)
            || responseType == ResponseType.FULL_DEFENCE;
    }

    private boolean isStatePaid(Response response) {
        ResponseType responseType = response.getResponseType();
        return responseType == ResponseType.FULL_DEFENCE
            && ((FullDefenceResponse) response).getDefenceType() == DefenceType.ALREADY_PAID;
    }

    private boolean isFullDefenseDisputeAcceptation(Response response, ClaimantResponse claimantResponse) {
        return claimantResponse.getType() == ACCEPTATION
            && ResponseUtils.isFullDefenceDispute(response);
    }

    private boolean shouldFormaliseResponseAcceptance(Response response, ClaimantResponse claimantResponse) {
        return ACCEPTATION == claimantResponse.getType()
            && !ResponseUtils.isResponseStatesPaid(response)
            && !ResponseUtils.isResponsePartAdmitPayImmediately(response);
    }

    private boolean isRejectResponseWithMediation(Claim claim, ClaimantResponse claimantResponse) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        return ClaimantResponseType.REJECTION.equals(claimantResponse.getType())
            && ((ResponseRejection) claimantResponse).getFreeMediation().filter(Predicate.isEqual(YesNoOption.YES))
            .isPresent()
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.YES)).isPresent()
            && (ResponseUtils.isPartAdmission(response)
            || ResponseUtils.isFullDefence(response)
            || ResponseUtils.isResponseStatesPaid(response));
    }
}
