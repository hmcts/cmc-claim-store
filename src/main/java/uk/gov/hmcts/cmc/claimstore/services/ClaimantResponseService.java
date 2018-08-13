package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

@Service
public class ClaimantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;
    private final AppInsights appInsights;
    private final CaseRepository caseRepository;
    private final ClaimantResponseRule claimantResponseRule;

    public ClaimantResponseService(
        EventProducer eventProducer,
        ClaimService claimService,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ClaimantResponseRule claimantResponseRule
    ) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
        this.appInsights = appInsights;
        this.caseRepository = caseRepository;
        this.claimantResponseRule = claimantResponseRule;
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

        caseRepository.saveClaimantResponse(claim.getId(), response, authorization);

        appInsights.trackEvent(getAppInsightsEvent(response), claim.getReferenceNumber());
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
