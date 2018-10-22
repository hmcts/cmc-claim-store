package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantRepaymentPlanRule;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;
    private final ClaimantRepaymentPlanRule claimantRepaymentPlanRule;
    private final AppInsights appInsights;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule,
        ClaimantRepaymentPlanRule claimantRepaymentPlanRule,
        AppInsights appInsights
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
        this.claimantRepaymentPlanRule = claimantRepaymentPlanRule;
        this.appInsights = appInsights;
    }

    @Transactional(transactionManager = "transactionManager")
    public Claim save(
        String submitterId,
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation,
        boolean issue
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, submitterId);

        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim, issue);

        if (countyCourtJudgment.getCcjType() != CountyCourtJudgmentType.ADMISSIONS &&
            countyCourtJudgment.getPaymentOption() == PaymentOption.INSTALMENTS) {
            claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
                countyCourtJudgment.getRepaymentPlan().orElse(null));

        }

        claimService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment, issue);

        Claim claimWithCCJ = claimService.getClaimByExternalId(externalId, authorisation);

        eventProducer.createCountyCourtJudgmentEvent(claimWithCCJ, authorisation, issue);

        appInsights.trackEvent(getAppInsightsEvent(issue), claim.getReferenceNumber());

        return claimWithCCJ;
    }

    private AppInsightsEvent getAppInsightsEvent(boolean issue) {
        if (issue) {
            return AppInsightsEvent.CCJ_ISSUED;
        } else {
            return AppInsightsEvent.CCJ_REQUESTED;
        }
    }
}
