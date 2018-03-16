package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class SettlementAgreementPDFContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;

    public SettlementAgreementPDFContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Offer acceptedOffer = claim.getSettlement().orElseThrow(IllegalArgumentException::new)
            .getLastOfferStatement().getOffer().orElseThrow(IllegalArgumentException::new);
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
            claim.getResponse().orElseThrow(IllegalStateException::new).getDefendant(),
            claim.getDefendantEmail()
        ));
        return content;
    }
}
