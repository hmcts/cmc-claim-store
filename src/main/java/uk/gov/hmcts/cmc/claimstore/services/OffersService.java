package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.time.LocalDateTime;

import static java.lang.String.format;

@Service
public class OffersService {

    private final ClaimService claimService;
    private final OffersRepository offersRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;

    @Autowired
    public OffersService(
        ClaimService claimService,
        OffersRepository offersRepository,
        EventProducer eventProducer,
        JsonMapper jsonMapper
    ) {
        this.claimService = claimService;
        this.offersRepository = offersRepository;
        this.eventProducer = eventProducer;
        this.jsonMapper = jsonMapper;
    }

    public void makeOffer(Claim claim, Offer offer, MadeBy party) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party);
        offersRepository.updateSettlement(claim.getId(), jsonMapper.toJson(settlement));
        eventProducer.createOfferMadeEvent(claim);
    }

    @Transactional
    public void accept(Claim claim, MadeBy party) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Offer has not been made yet."));
        settlement.accept(party);

        offersRepository.acceptOffer(claim.getId(), jsonMapper.toJson(settlement), LocalDateTime.now());
        eventProducer.createOfferAcceptedEvent(claimService.getClaimById(claim.getId()), party);
    }

    public void reject(Claim claim, MadeBy party) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Offer has not been made yet."));
        settlement.reject(party);

        offersRepository.updateSettlement(claim.getId(), jsonMapper.toJson(settlement));
        eventProducer.createOfferRejectedEvent(claim, party);
    }

    private void assertSettlementIsNotReached(final Claim claim) {
        if (claim.getSettlementReachedAt() != null) {
            throw new ConflictException(format("Settlement for claim %d has been already reached", claim.getId()));
        }
    }
}
