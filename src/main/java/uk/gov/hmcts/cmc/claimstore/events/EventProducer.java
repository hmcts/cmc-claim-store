package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRejectedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.time.LocalDate;

@Component
public class EventProducer {
    private final ApplicationEventPublisher publisher;

    public EventProducer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createClaimIssuedEvent(Claim claim, String pin,
                                       String submitterName, String authorisation) {

        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimIssuedEvent(claim, submitterName, authorisation));
        } else {
            publisher.publishEvent(new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation));
        }
    }

    public void createDefendantResponseEvent(Claim claim) {
        publisher.publishEvent(new DefendantResponseEvent(claim));
    }

    public void createMoreTimeForResponseRequestedEvent(
        Claim claim, LocalDate newResponseDeadline, String defendantEmail) {
        publisher.publishEvent(new MoreTimeRequestedEvent(claim, newResponseDeadline, defendantEmail));
    }

    public void createCountyCourtJudgmentEvent(
        Claim claim,
        String authorisation,
        CountyCourtJudgmentType countyCourtJudgmentType) {
        publisher.publishEvent(new CountyCourtJudgmentEvent(claim, authorisation, countyCourtJudgmentType));
    }

    public void createOfferMadeEvent(Claim claim) {
        publisher.publishEvent(new OfferMadeEvent(claim));
    }

    public void createOfferAcceptedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new OfferAcceptedEvent(claim, party));
    }

    public void createOfferRejectedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new OfferRejectedEvent(claim, party));
    }

    public void createAgreementCountersignedEvent(Claim claim, MadeBy party) {
        publisher.publishEvent(new AgreementCountersignedEvent(claim, party));
    }

    public void createSignSettlementAgreementEvent(Claim claim) {
        publisher.publishEvent(new SignSettlementAgreementEvent(claim));
    }

    public void createClaimantResponseEvent(Claim claim) {
        publisher.publishEvent(new ClaimantResponseEvent(claim));
    }

    public void createPaidInFullEvent(Claim claim) {
        publisher.publishEvent(new PaidInFullEvent(claim));
    }
}
