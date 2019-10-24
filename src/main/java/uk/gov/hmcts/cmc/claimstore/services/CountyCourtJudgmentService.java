package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED_BY_ADMISSION;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.REDETERMINATION_REQUESTED;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;
    private final UserService userService;
    private final AppInsights appInsights;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule,
        UserService userService,
        AppInsights appInsights
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
        this.userService = userService;
        this.appInsights = appInsights;
    }

    public Claim save(
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation

    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, userDetails.getId());

        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim, countyCourtJudgment.getCcjType());

        claimService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);

        Claim claimWithCCJ = claimService.getClaimByExternalId(externalId, authorisation);

        eventProducer.createCountyCourtJudgmentEvent(claimWithCCJ, authorisation);

        AppInsightsEvent appInsightsEvent = CCJ_REQUESTED;
        if (countyCourtJudgment.getCcjType() == CountyCourtJudgmentType.ADMISSIONS) {
            appInsightsEvent = CCJ_REQUESTED_BY_ADMISSION;
        }

        if (countyCourtJudgmentRule.isCCJDueToSettlementBreach(claimWithCCJ)) {
            appInsightsEvent = CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH;
        }

        appInsights.trackEvent(appInsightsEvent, AppInsights.REFERENCE_NUMBER, claim.getReferenceNumber());

        return claimWithCCJ;
    }

    public Claim reDetermination(
        ReDetermination redetermination,
        String externalId,
        String authorisation
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsParticipantOnClaim(claim, userDetails.getId());
        countyCourtJudgmentRule.assertRedeterminationCanBeRequestedOnCountyCourtJudgement(claim);

        claimService.saveReDetermination(authorisation, claim, redetermination);

        Claim claimWithReDetermination = claimService.getClaimByExternalId(externalId, authorisation);

        eventProducer.createRedeterminationEvent(
            claimWithReDetermination,
            authorisation,
            userDetails.getFullName(),
            redetermination.getPartyType()
        );

        appInsights.trackEvent(REDETERMINATION_REQUESTED, AppInsights.REFERENCE_NUMBER, claim.getReferenceNumber());

        return claimWithReDetermination;

    }
}
