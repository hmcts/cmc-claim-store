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
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_AGREEMENT_REACHED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.SETTLEMENT_AGREEMENT_REJECTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
@Transactional(transactionManager = "transactionManager")
public class SettlementAgreementService {

    private static final String REJECTION_EXPECTED_STATE_ERROR =
        "Expecting last statement for claim %d to be ACCEPTATION from CLAIMANT";


    private final ClaimService claimService;
    private final CaseRepository caseRepository;
    private final EventProducer eventProducer;
    private final AppInsights appInsights;

    @Autowired
    public SettlementAgreementService(
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

    public Claim reject(Claim claim, String authorisation) {

        Settlement settlement = assertSettlementCanBeResponded(claim);
        settlement.reject(MadeBy.DEFENDANT);


        String userAction = format("SETTLEMENT_AGREEMENT_REJECTED_BY_%s", MadeBy.DEFENDANT.name());
        caseRepository.updateSettlement(claim, settlement, authorisation, userAction);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);

        eventProducer.createSettlementAgreementRejectedEvent(updated);
        appInsights.trackEvent(SETTLEMENT_AGREEMENT_REJECTED, REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;
    }

    public Claim countersign(Claim claim, String authorisation) {

        Settlement settlement = assertSettlementCanBeResponded(claim);
        settlement.countersign(MadeBy.DEFENDANT);

        String userAction = format("SETTLEMENT_AGREEMENT_COUNTERSIGNED_BY_%s", MadeBy.DEFENDANT.name());
        caseRepository.updateSettlement(claim, settlement, authorisation, userAction);

        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);

        eventProducer.createSettlementAgreementCountersignedEvent(updated);
        appInsights.trackEvent(SETTLEMENT_AGREEMENT_REACHED, AppInsights.REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;

    }

    private Settlement assertSettlementCanBeResponded(Claim claim) {
        assertSettlementIsNotReached(claim);
        assertLastStatementIsClaimantAcceptation(claim);

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Settlement agreement has not yet been made."));

        return settlement;
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
