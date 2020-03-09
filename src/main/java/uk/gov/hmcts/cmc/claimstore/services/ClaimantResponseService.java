package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.function.Predicate.isEqual;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LIFT_STAY;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BOTH_OPTED_IN_FOR_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BOTH_OPTED_IN_FOR_NON_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_OPTED_OUT_FOR_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_RESPONSE_ACCEPTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_NON_PILOT_ELIGIBLE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_PILOT_ELIGIBLE;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isSettlePreJudgment;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.hasMediationPilotFeature;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.hasDefendantOptedForMediation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isFullDefence;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isFullDefenceDispute;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isPartAdmission;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponsePartAdmitPayImmediately;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaid;

@Service
public class ClaimantResponseService {

    private final ClaimService claimService;
    private final AppInsights appInsights;
    private final CaseRepository caseRepository;
    private final ClaimantResponseRule claimantResponseRule;
    private final EventProducer eventProducer;
    private final FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;
    private final DirectionsQuestionnaireService directionsQuestionnaireService;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;

    @SuppressWarnings("squid:S00107") // All parameters are required here
    public ClaimantResponseService(
        ClaimService claimService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ClaimantResponseRule claimantResponseRule,
        EventProducer eventProducer,
        FormaliseResponseAcceptanceService formaliseResponseAcceptanceService,
        DirectionsQuestionnaireService directionsQuestionnaireService,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator
    ) {
        this.claimService = claimService;
        this.appInsights = appInsights;
        this.caseRepository = caseRepository;
        this.claimantResponseRule = claimantResponseRule;
        this.eventProducer = eventProducer;
        this.formaliseResponseAcceptanceService = formaliseResponseAcceptanceService;
        this.directionsQuestionnaireService = directionsQuestionnaireService;
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

        Response response = claim.getResponse().orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE));
        if (claim.getState().equals(ClaimState.STAYED) && ResponseUtils.isAdmissionResponse(response)) {
            claim = caseRepository.saveCaseEvent(authorization, claim, LIFT_STAY);
        }

        Claim updatedClaim = caseRepository.saveClaimantResponse(claim, claimantResponse, authorization);

        claimantResponseRule.isValid(updatedClaim);
        formaliseResponseAcceptance(claimantResponse, response, updatedClaim, authorization);

        if (isFullDefenseDisputeAcceptation(response, claimantResponse)) {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, updatedClaim.getReferenceNumber());

            caseRepository.saveCaseEvent(authorization, updatedClaim, CaseEvent.STAY_CLAIM);
        }

        if (!FeaturesUtils.isOnlineDQ(updatedClaim) && isRejectResponseNoMediation(claimantResponse)) {
            LocalDate deadline = directionsQuestionnaireDeadlineCalculator
                .calculateDirectionsQuestionnaireDeadline(claim.getRespondedAt());
            caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
            updatedClaim = claimService.getClaimByExternalId(externalId, authorization);
        }

        if (!isSettlementAgreement(response, claimantResponse)) {
            eventProducer.createClaimantResponseEvent(updatedClaim, authorization);
        }

        if (isSettlePreJudgment(claimantResponse)) {
            caseRepository.saveCaseEvent(authorization, updatedClaim, SETTLED_PRE_JUDGMENT);
        }

        Optional<CaseEvent> caseEvent = Optional.empty();
        if (claimantResponse.getType() == REJECTION) {
            caseEvent = directionsQuestionnaireService.prepareCaseEvent(
                (ResponseRejection) claimantResponse,
                updatedClaim
            );
            if (caseEvent.isPresent()) {
                caseRepository.saveCaseEvent(authorization, updatedClaim, caseEvent.get());
            }
        }

        raiseAppInsightEvents(updatedClaim, response, claimantResponse, caseEvent.orElseGet(() -> null));
    }

    private boolean isSettlementAgreement(Response response, ClaimantResponse claimantResponse) {

        if (shouldFormaliseResponseAcceptance(response, claimantResponse)) {
            return ((ResponseAcceptation) claimantResponse).getFormaliseOption()
                .filter(isEqual(FormaliseOption.SETTLEMENT))
                .isPresent();
        }
        return false;
    }

    private boolean isRejectResponseNoMediation(ClaimantResponse claimantResponse) {
        return ClaimantResponseType.REJECTION.equals(claimantResponse.getType())
            && ((ResponseRejection) claimantResponse).getFreeMediation()
            .filter(isEqual(YesNoOption.NO))
            .isPresent();
    }

    private void formaliseResponseAcceptance(
        ClaimantResponse claimantResponse,
        Response response,
        Claim claim,
        String authorization
    ) {
        if (shouldFormaliseResponseAcceptance(response, claimantResponse)) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
            if (responseAcceptation.getFormaliseOption().isPresent()) {
                formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, authorization);
            }
        }
    }

    private void raiseAppInsightEvents(Claim claim,
                                        Response response,
                                        ClaimantResponse claimantResponse,
                                        CaseEvent caseEvent) {

        if (claimantResponse instanceof ResponseAcceptation) {
            appInsights.trackEvent(CLAIMANT_RESPONSE_ACCEPTED, REFERENCE_NUMBER, claim.getReferenceNumber());

        } else if (claimantResponse instanceof ResponseRejection) {
            if (isPartAdmissionOrIsStatePaidOrIsFullDefence(response)) {
                raiseAppInsightEventForOnlineOrOfflineDQ(claim);
                raiseAppInsightEventForMediation(claim, response, (ResponseRejection) claimantResponse);

                if (caseEvent != null) {
                    raiseAppInsightsEventForPilot(claim, caseEvent);
                }
            }
        } else {
            throw new IllegalStateException("Unknown response type");
        }
    }

    private void raiseAppInsightsEventForPilot(Claim claim, CaseEvent caseEvent) {
        switch (caseEvent) {
            case ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS:
                appInsights.trackEvent(AppInsightsEvent.LA_PILOT_ELIGIBLE, REFERENCE_NUMBER,
                    claim.getReferenceNumber());
                break;
            case ASSIGNING_FOR_JUDGE_DIRECTIONS:
                appInsights.trackEvent(AppInsightsEvent.JDDO_PILOT_ELIGIBLE, REFERENCE_NUMBER,
                    claim.getReferenceNumber());
                break;
            case WAITING_TRANSFER:
                appInsights.trackEvent(AppInsightsEvent.READY_FOR_TRANSFER, REFERENCE_NUMBER,
                        claim.getReferenceNumber());
                break;
            default:
                //Empty
                break;
        }
    }

    private void raiseAppInsightEventForOnlineOrOfflineDQ(Claim claim) {
        AppInsightsEvent appInsightsEvent = FeaturesUtils.isOnlineDQ(claim)
            ? AppInsightsEvent.BOTH_PARTIES_ONLINE_DQ
            : AppInsightsEvent.BOTH_PARTIES_OFFLINE_DQ;

        appInsights.trackEvent(appInsightsEvent, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    private void raiseAppInsightEventForMediation(Claim claim, Response response, ResponseRejection responseRejection) {
        String referenceNumber = claim.getReferenceNumber();
        boolean claimantNotOptedForMediation = responseRejection.getFreeMediation().filter(isEqual(NO)).isPresent();

        if (claimantNotOptedForMediation) {
            appInsights.trackEvent(getAppInsightEventBasedOnMediationPilot(claim), REFERENCE_NUMBER, referenceNumber);
        } else if (hasBothOptedForMediation(response, responseRejection)) {
            if (hasMediationPilotFeature(claim)) {
                appInsights.trackEvent(MEDIATION_PILOT_ELIGIBLE, REFERENCE_NUMBER, referenceNumber);
                appInsights.trackEvent(BOTH_OPTED_IN_FOR_MEDIATION_PILOT, REFERENCE_NUMBER, referenceNumber);
            } else {
                appInsights.trackEvent(BOTH_OPTED_IN_FOR_NON_MEDIATION_PILOT, REFERENCE_NUMBER, referenceNumber);
                appInsights.trackEvent(MEDIATION_NON_PILOT_ELIGIBLE, REFERENCE_NUMBER, referenceNumber);
            }
        }
    }

    private AppInsightsEvent getAppInsightEventBasedOnMediationPilot(Claim claim) {
        return hasMediationPilotFeature(claim)
            ? CLAIMANT_OPTED_OUT_FOR_MEDIATION_PILOT
            : CLAIMANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT;
    }

    private boolean hasBothOptedForMediation(Response response, ResponseRejection responseRejection) {
        boolean claimantOptedForMediation = responseRejection.getFreeMediation().filter(isEqual(YES)).isPresent();
        return claimantOptedForMediation && hasDefendantOptedForMediation(response);
    }

    private boolean isPartAdmissionOrIsStatePaidOrIsFullDefence(Response response) {
        return isPartAdmission(response) || isFullDefence(response) || isResponseStatesPaid(response);
    }

    private boolean isFullDefenseDisputeAcceptation(Response response, ClaimantResponse claimantResponse) {
        return claimantResponse.getType() == ACCEPTATION && isFullDefenceDispute(response);
    }

    private boolean shouldFormaliseResponseAcceptance(Response response, ClaimantResponse claimantResponse) {
        return ACCEPTATION == claimantResponse.getType()
            && !isResponseStatesPaid(response)
            && !isResponsePartAdmitPayImmediately(response);
    }
}
