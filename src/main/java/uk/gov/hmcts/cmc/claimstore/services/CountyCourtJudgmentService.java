package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import static uk.gov.hmcts.cmc.ccd.domain.CaseState.OPEN;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
    }

    @Transactional
    public Claim save(
        String submitterId,
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation
    ) {

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation, OPEN);

        authorisationService.assertIsSubmitterOnClaim(claim, submitterId);

        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);

        claimService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);

        Claim claimWithCCJ = claimService.getClaimByExternalId(externalId, authorisation, OPEN);

        eventProducer.createCountyCourtJudgmentRequestedEvent(claimWithCCJ, authorisation);

        return claimWithCCJ;
    }

}
