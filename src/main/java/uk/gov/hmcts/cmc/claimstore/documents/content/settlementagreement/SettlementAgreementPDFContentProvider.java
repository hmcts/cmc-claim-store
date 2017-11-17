package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class SettlementAgreementPDFContentProvider {

    private final DefendantDetailsContentProvider defendantDetailsContentProvider;

    public SettlementAgreementPDFContentProvider(
        final DefendantDetailsContentProvider defendantDetailsContentProvider
    ) {
        this.defendantDetailsContentProvider = defendantDetailsContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);

        Offer acceptedOffer = claim.getSettlement().orElseThrow(IllegalArgumentException::new)
            .getLastStatementWithOffer().getOffer().orElseThrow(IllegalArgumentException::new);
        Map<String, Object> content = new HashMap<>();
        content.put("settlementReachedAt", formatDateTime(claim.getSettlementReachedAt()));
        content.put("acceptedOffer", acceptedOffer.getContent());
        content.put("acceptedOfferCompletionDate", formatDate(acceptedOffer.getCompletionDate()));
        content.put("claim", claim);
        content.put("claimant", claim.getClaimData().getClaimant());
        content.put("defendant", defendantDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            claim.getResponse().orElseThrow(IllegalStateException::new),
            claim.getDefendantEmail()
        ));
        return content;
    }
}
