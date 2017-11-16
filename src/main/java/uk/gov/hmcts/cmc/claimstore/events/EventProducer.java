package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRejectedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class EventProducer {
    private final ApplicationEventPublisher publisher;

    public EventProducer(final ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createClaimIssuedEvent(final Claim claim, final String pin,
                                       final String submitterName, final String authorisation) {

        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimIssuedEvent(claim, Optional.of(submitterName), authorisation));
        } else {
            publisher.publishEvent(
                new ClaimIssuedEvent(claim, Optional.of(pin), Optional.of(submitterName), authorisation)
            );
        }
    }

    public void createDefendantResponseEvent(final Claim claim) {
        publisher.publishEvent(new DefendantResponseEvent(claim));
    }

    public void createMoreTimeForResponseRequestedEvent(
        final Claim claim, final LocalDate newResponseDeadline, final String defendantEmail) {
        publisher.publishEvent(new MoreTimeRequestedEvent(claim, newResponseDeadline, defendantEmail));
    }

    public void createCountyCourtJudgmentRequestedEvent(final Claim claim) {
        publisher.publishEvent(new CountyCourtJudgmentRequestedEvent(claim));
    }

    public void createOfferMadeEvent(final Claim claim) {
        publisher.publishEvent(new OfferMadeEvent(claim));
    }

    public void createOfferAcceptedEvent(final Claim claim, final MadeBy party) {
        publisher.publishEvent(new OfferAcceptedEvent(claim, party));
    }

    public void createOfferRejectedEvent(final Claim claim, final MadeBy party) {
        publisher.publishEvent(new OfferRejectedEvent(claim, party));
    }
}
