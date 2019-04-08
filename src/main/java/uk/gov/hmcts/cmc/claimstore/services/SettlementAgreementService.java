package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_REJECTED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_SIGNED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_RESPONSE_GENERATED_OFFER_MADE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_AGREEMENT_REACHED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_AGREEMENT_REACHED_BY_ADMISSION;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_AGREEMENT_REJECTED;

@Service
@Transactional(transactionManager = "transactionManager")
public class SettlementAgreementService {

    private static final String REJECTION_EXPECTED_STATE_ERROR =
        "Expecting last statement for claim %d to be ACCEPTATION from CLAIMANT";

    private final ClaimService claimService;
    private final CaseRepository caseRepository;
    private final EventProducer eventProducer;
    private final AppInsights appInsights;
    private final CCDEventProducer ccdEventProducer;

    @Autowired
    public SettlementAgreementService(
        ClaimService claimService,
        CaseRepository caseRepository,
        EventProducer eventProducer,
        AppInsights appInsights,
        CCDEventProducer ccdEventProducer
    ) {
        this.claimService = claimService;
        this.caseRepository = caseRepository;
        this.eventProducer = eventProducer;
        this.appInsights = appInsights;
        this.ccdEventProducer = ccdEventProducer;
    }

    public Claim reject(Claim claim, String authorisation) {

        Settlement settlement = assertSettlementCanBeResponded(claim);
        settlement.reject(MadeBy.DEFENDANT, null);

        CaseEvent caseEvent = AGREEMENT_REJECTED_BY_DEFENDANT;
        caseRepository.updateSettlement(claim, settlement, authorisation, caseEvent);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);

        eventProducer.createRejectSettlementAgreementEvent(updated);
        this.ccdEventProducer.createCCDSettlementEvent(claim, settlement, authorisation, caseEvent);
        appInsights.trackEvent(SETTLEMENT_AGREEMENT_REJECTED, REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;
    }

    public Claim countersign(Claim claim, String authorisation) {

        Settlement settlement = assertSettlementCanBeResponded(claim);
        settlement.countersign(MadeBy.DEFENDANT, null);

        CaseEvent caseEvent = AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT;
        caseRepository.reachSettlementAgreement(claim, settlement, authorisation, caseEvent);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);

        eventProducer.createSettlementAgreementCountersignedEvent(updated, authorisation);
        this.ccdEventProducer.createCCDSettlementEvent(claim, settlement, authorisation, caseEvent);
        AppInsightsEvent appInsightsEvent = settlement.isSettlementThroughAdmissions()
            ? SETTLEMENT_AGREEMENT_REACHED_BY_ADMISSION : SETTLEMENT_AGREEMENT_REACHED;
        appInsights.trackEvent(appInsightsEvent, REFERENCE_NUMBER, updated.getReferenceNumber());

        return updated;

    }

    protected void signSettlementAgreement(String externalId, Settlement settlement, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        assertSettlementIsNotReached(claim);
        CaseEvent caseEvent = AGREEMENT_SIGNED_BY_CLAIMANT;
        this.caseRepository.updateSettlement(claim, settlement, authorisation, caseEvent);

        final Claim signedSettlementClaim = this.claimService.getClaimByExternalId(externalId, authorisation);
        this.eventProducer.createSignSettlementAgreementEvent(signedSettlementClaim);
        this.ccdEventProducer.createCCDSettlementEvent(claim, settlement, authorisation, caseEvent);
        appInsights.trackEvent(CLAIMANT_RESPONSE_GENERATED_OFFER_MADE,
            REFERENCE_NUMBER, signedSettlementClaim.getReferenceNumber());
    }

    private Settlement assertSettlementCanBeResponded(Claim claim) {
        assertSettlementIsNotReached(claim);
        assertLastStatementIsClaimantAcceptation(claim);

        return claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Settlement agreement has not yet been made."));

    }

    private void assertSettlementIsNotReached(Claim claim) {
        if (claim.getSettlementReachedAt() != null) {
            throw new ConflictException(format("Settlement for claim %d has been already reached", claim.getId()));
        }
    }

    private void assertLastStatementIsClaimantAcceptation(Claim claim) {
        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException(format(REJECTION_EXPECTED_STATE_ERROR, claim.getId())));

        PartyStatement lastStatement = settlement.getLastStatement();

        if (lastStatement.getType() != StatementType.ACCEPTATION) {
            throw new ConflictException(
                format(REJECTION_EXPECTED_STATE_ERROR, claim.getId()));
        }
    }
}
