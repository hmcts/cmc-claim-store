package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;
    private final AppInsights appInsights;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule,
        AppInsights appInsights
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
        this.appInsights = appInsights;
    }

    @Transactional(transactionManager = "transactionManager")
    public Claim save(
        String submitterId,
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation,
        boolean isByAdmission
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, submitterId);

        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim, isByAdmission);

        claimService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment, isByAdmission);

        Claim claimWithCCJ = claimService.getClaimByExternalId(externalId, authorisation);

        eventProducer.createCountyCourtJudgmentEvent(claimWithCCJ, authorisation, isByAdmission);

        appInsights.trackEvent(getAppInsightsEvent(isByAdmission), claim.getReferenceNumber());

        return claimWithCCJ;
    }

    private AppInsightsEvent getAppInsightsEvent(boolean isByAdmission) {
        if (isByAdmission) {
            return AppInsightsEvent.CCJ_ISSUED;
        } else {
            return AppInsightsEvent.CCJ_REQUESTED;
        }
    }
}
