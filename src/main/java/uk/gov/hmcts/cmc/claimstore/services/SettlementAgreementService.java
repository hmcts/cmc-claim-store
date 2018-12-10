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
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
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
        assertSettlementIsNotReached(claim);
        assertLastStatementIsClaimantAcceptation(claim);

        MadeBy party = MadeBy.DEFENDANT;

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new ConflictException("Settlement agreement has not yet been made."));

        settlement.reject(party);

        String userAction = format("SETTLEMENT_AGREEMENT_REJECTED_BY_%s", party.name());
        caseRepository.updateSettlement(claim, settlement, authorisation, userAction);
        Claim updated = claimService.getClaimByExternalId(claim.getExternalId(), authorisation);
        eventProducer.createRejectSettlementAgreementEvent(updated);
        appInsights.trackEvent(SETTLEMENT_AGREEMENT_REJECTED, REFERENCE_NUMBER, updated.getReferenceNumber());
        return updated;
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
