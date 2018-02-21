package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.services.search.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import static java.lang.String.format;

@Service
public class OffersService {

    private final ClaimService claimService;
    private final CaseRepository caseRepository;
    private final EventProducer eventProducer;

    @Autowired
    public OffersService(
        ClaimService claimService,
        CaseRepository caseRepository,
        EventProducer eventProducer
    ) {
        this.claimService = claimService;
        this.caseRepository = caseRepository;
        this.eventProducer = eventProducer;
    }

    public void makeOffer(Claim claim, Offer offer, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party);

        caseRepository.updateSettlement(claim, settlement, authorisation, userAcion("OFFER_MADE_BY", party.name()));
        eventProducer.createOfferMadeEvent(claim);
    }

    public void accept(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Offer has not been made yet."));

        settlement.accept(party);

        caseRepository.updateSettlement(claim, settlement, authorisation,
            userAcion("OFFER_ACCEPTED_BY", party.name()));
        eventProducer.createOfferAcceptedEvent(claim, party);
    }

    public void reject(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Offer has not been made yet."));
        settlement.reject(party);

        caseRepository.updateSettlement(claim, settlement, authorisation, userAcion("OFFER_REJECTED_BY", party.name()));

        eventProducer.createOfferRejectedEvent(claim, party);
    }

    @Transactional
    public void countersign(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Offer has not been made yet."));
        settlement.countersign(party);

        caseRepository.reachSettlementAgreement(claim, settlement, authorisation,
            userAcion("OFFER_COUNTERSIGNED_BY", party.name()));

        eventProducer.createAgreementCountersignedEvent(claimService.getClaimById(claim.getId()), party);
    }

    private void assertSettlementIsNotReached(Claim claim) {
        if (claim.getSettlementReachedAt() != null) {
            throw new ConflictException(format("Settlement for claim %d has been already reached", claim.getId()));
        }
    }

    private String userAcion(String userAction, String userType) {
        return userAction + "_" + userType;
    }
}
