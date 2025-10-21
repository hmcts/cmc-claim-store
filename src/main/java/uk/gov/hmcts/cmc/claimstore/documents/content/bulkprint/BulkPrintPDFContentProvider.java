package uk.gov.hmcts.cmc.claimstore.documents.content.bulkprint;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
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
public class BulkPrintPDFContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;

    public BulkPrintPDFContentProvider(
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
        content.put("claimant", claim.getClaimData().getClaimant());
        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getResponse()
                .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE))
                .getDefendant(),
            claim.getDefendantEmail()
        ));
        return content;
    }
}
