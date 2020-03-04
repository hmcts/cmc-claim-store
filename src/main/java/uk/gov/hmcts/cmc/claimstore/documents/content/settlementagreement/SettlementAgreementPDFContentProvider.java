package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_OFFER;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_SETTLEMENT;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class SettlementAgreementPDFContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;

    private static final String SETTLEMENT_FORM_NAME_OFFERS_ROUTE = "OCON Settlement Agreement";
    private static final String SETTLEMENT_FORM_NAME_ADMISSIONS_ROUTE = "OCON Settlement Agreement A";

    public SettlementAgreementPDFContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Offer acceptedOffer = claim.getSettlement()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_SETTLEMENT))
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_OFFER));
        Map<String, Object> content = new HashMap<>();
        content.put("settlementReachedAt", formatDateTime(claim.getSettlementReachedAt()));
        content.put("acceptedOffer", acceptedOffer.getContent());
        content.put("acceptedOfferCompletionDate", formatDate(acceptedOffer.getCompletionDate()));
        content.put("claim", claim);
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));
        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getResponse()
                .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE))
                .getDefendant(),
            claim.getDefendantEmail()
        ));

        Settlement settlement = claim.getSettlement()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_SETTLEMENT));
        if (settlement.isSettlementThroughAdmissions()) {
            content.put("formName", SETTLEMENT_FORM_NAME_ADMISSIONS_ROUTE);
        } else {
            content.put("formName", SETTLEMENT_FORM_NAME_OFFERS_ROUTE);
        }

        return content;
    }
}
