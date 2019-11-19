package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
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
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_MADE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_MADE_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_REJECTED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_REJECTED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_SIGNED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
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

    public Claim makeOffer(Claim claim, Offer offer, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party, null);

        CaseEvent caseEvent =
            party == MadeBy.CLAIMANT ? OFFER_MADE_BY_CLAIMANT : OFFER_MADE_BY_DEFENDANT;

        caseRepository.updateSettlement(claim, settlement, authorisation, caseEvent);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferMadeEvent(updated);
        appInsights.trackEvent(OFFER_MADE, REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;
    }

    public Claim accept(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());

        settlement.accept(party, null);

        caseRepository.updateSettlement(claim, settlement, authorisation, OFFER_SIGNED_BY_CLAIMANT);
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferAcceptedEvent(updated, party);
        return updated;
    }

    public Claim reject(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());
        settlement.reject(party, null);

        CaseEvent caseEvent
            = party == MadeBy.CLAIMANT ? OFFER_REJECTED_BY_CLAIMANT : OFFER_REJECTED_BY_DEFENDANT;
        caseRepository.updateSettlement(claim, settlement, authorisation, caseEvent);
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createOfferRejectedEvent(updated, party);
        appInsights.trackEvent(OFFER_REJECTED, REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;
    }

    public Claim countersign(Claim claim, MadeBy party, String authorisation) {
        assertSettlementIsNotReached(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(conflictOfferIsNotMade());
        settlement.countersign(party, null);

        caseRepository.reachSettlementAgreement(claim, settlement, authorisation,
            OFFER_COUNTER_SIGNED_BY_DEFENDANT);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createAgreementCountersignedEvent(updated, party, authorisation);
        appInsights.trackEvent(SETTLEMENT_REACHED, REFERENCE_NUMBER, updated.getReferenceNumber());
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
}
