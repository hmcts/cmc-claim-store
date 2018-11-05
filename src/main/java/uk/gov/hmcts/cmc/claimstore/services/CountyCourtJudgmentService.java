package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantRepaymentPlanRule;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;
    private final ClaimantRepaymentPlanRule claimantRepaymentPlanRule;
    private final UserService userService;
    private final AppInsights appInsights;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule,
        ClaimantRepaymentPlanRule claimantRepaymentPlanRule,
        UserService userService,
        AppInsights appInsights
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
        this.claimantRepaymentPlanRule = claimantRepaymentPlanRule;
        this.userService = userService;
        this.appInsights = appInsights;
    }

    @Transactional(transactionManager = "transactionManager")
    public Claim save(
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation,
        boolean issue
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, userDetails.getId());

        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim, issue);

        if (countyCourtJudgment.getCcjType() != CountyCourtJudgmentType.DEFAULT
            && countyCourtJudgment.getPaymentOption() != PaymentOption.IMMEDIATELY) {
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

    public Claim reDetermination(
        ReDetermination redetermination,
        String externalId,
        String authorisation
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, userDetails.getId());
        countyCourtJudgmentRule.assertRedeterminationCanBeRequestedOnCountyCourtJudgement(claim);

        claimService.saveReDetermination(authorisation, claim, redetermination, userDetails.getId());

        Claim claimWithReDetermination = claimService.getClaimByExternalId(externalId, authorisation);

        eventProducer.createRedeterminationEvent(claimWithReDetermination, authorisation, userDetails.getFullName());

        return claimWithReDetermination;

    }
}
