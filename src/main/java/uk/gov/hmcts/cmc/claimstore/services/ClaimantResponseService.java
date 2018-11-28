package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;

import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;

@Service
public class ClaimantResponseService {

    private final ClaimService claimService;
    private final AppInsights appInsights;
    private final CaseRepository caseRepository;
    private final ClaimantResponseRule claimantResponseRule;
    private final EventProducer eventProducer;
    private final FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;
    private CCDEventProducer ccdEventProducer;

    public ClaimantResponseService(
        ClaimService claimService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ClaimantResponseRule claimantResponseRule,
        EventProducer eventProducer,
        FormaliseResponseAcceptanceService formaliseResponseAcceptanceService,
        CCDEventProducer ccdEventProducer) {
        this.claimService = claimService;
        this.appInsights = appInsights;
        this.caseRepository = caseRepository;
        this.claimantResponseRule = claimantResponseRule;
        this.eventProducer = eventProducer;
        this.formaliseResponseAcceptanceService = formaliseResponseAcceptanceService;
        this.ccdEventProducer = ccdEventProducer;
    }

    @Transactional(transactionManager = "transactionManager")
    public void save(
        String externalId,
        String claimantId,
        ClaimantResponse response,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);
        claimantResponseRule.assertCanBeRequested(claim, claimantId);

        Claim updatedClaim = caseRepository.saveClaimantResponse(claim, response, authorization);

        formaliseResponseAcceptance(response, updatedClaim, authorization);

        eventProducer.createClaimantResponseEvent(updatedClaim);
        ccdEventProducer.createCCDClaimantResponseEvent(claim, response, authorization);

        appInsights.trackEvent(getAppInsightsEvent(response), "referenceNumber", claim.getReferenceNumber());
    }

    private void formaliseResponseAcceptance(ClaimantResponse claimantResponse, Claim claim, String authorization) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        if (ACCEPTATION == claimantResponse.getType()
            && !ResponseUtils.isResponseStatesPaid(response)) {
            formaliseResponseAcceptanceService.formalise(claim, (ResponseAcceptation) claimantResponse, authorization);
        }
    }

    private AppInsightsEvent getAppInsightsEvent(ClaimantResponse claimantResponse) {
        if (claimantResponse instanceof ResponseAcceptation) {
            return AppInsightsEvent.CLAIMANT_RESPONSE_ACCEPTED;
        } else if (claimantResponse instanceof ResponseRejection) {
            return AppInsightsEvent.CLAIMANT_RESPONSE_REJECTED;
        } else {
            throw new IllegalStateException("Unknown response type");
        }
    }
}
