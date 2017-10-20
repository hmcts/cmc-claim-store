package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.time.LocalDate;

@Component
public class EventProducer {
    private final ApplicationEventPublisher publisher;

    public EventProducer(final ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void createClaimIssuedEvent(final Claim claim, final String pin, final String submitterName) {
        if (claim.getClaimData().isClaimantRepresented()) {
            publisher.publishEvent(new RepresentedClaimIssuedEvent(claim, submitterName));
        } else {
            publisher.publishEvent(new ClaimIssuedEvent(claim, pin, submitterName));
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
}
