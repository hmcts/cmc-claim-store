package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.OFFER_MADE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.OFFER_REJECTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_REACHED;

@Service
public class OffersService {

    private final ClaimService claimService;
    private final CaseRepository caseRepository;
    private final EventProducer eventProducer;
    private final AppInsights appInsights;

    @Autowired
    public OffersService(
        ClaimService claimService,
        CaseRepository caseRepository,
        EventProducer eventProducer,
        AppInsights appInsights
    ) {
        this.claimService = claimService;
        this.caseRepository = caseRepository;
        this.eventProducer = eventProducer;
        this.appInsights = appInsights;
    }

    @Transactional
    public Claim makeOffer(Claim claim, Offer offer, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party);

        caseRepository.updateSettlement(claim, settlement, authorisation, userAction("OFFER_MADE_BY", party.name()));
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferMadeEvent(updated);
        appInsights.trackEvent(OFFER_MADE, updated.getReferenceNumber());
        return updated;
    }

    @Transactional
    public Claim accept(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());

        settlement.accept(party);

        caseRepository.updateSettlement(claim, settlement, authorisation,
            userAction("OFFER_ACCEPTED_BY", party.name()));

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferAcceptedEvent(updated, party);
        return updated;
    }

    @Transactional
    public Claim reject(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());
        settlement.reject(party);

        String userAction = userAction("OFFER_REJECTED_BY", party.name());
        caseRepository.updateSettlement(claim, settlement, authorisation, userAction);
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferRejectedEvent(updated, party);
        appInsights.trackEvent(OFFER_REJECTED, updated.getReferenceNumber());
        return updated;
    }

    @Transactional
    public Claim countersign(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());
        settlement.countersign(party);

        caseRepository.reachSettlementAgreement(claim, settlement, authorisation, SETTLED_PRE_JUDGMENT.name());
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createAgreementCountersignedEvent(updated, party);
        appInsights.trackEvent(SETTLEMENT_REACHED, updated.getReferenceNumber());
        return updated;
    }

    private Supplier<ConflictException> conflictOfferIsNotMade() {
        return () -> new ConflictException("Offer has not been made yet.");
    }

    private void assertSettlementIsNotReached(Claim claim) {
        if (claim.getSettlementReachedAt() != null) {
            throw new ConflictException(format("Settlement for claim %d has been already reached", claim.getId()));
        }
    }

    private String userAction(String userAction, String userType) {
        return userAction + "_" + userType;
    }
}
