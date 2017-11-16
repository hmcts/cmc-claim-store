package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
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
        Map<String, Object> content = new HashMap<>();
        content.put("settlementReachedAt", formatDateTime(claim.getSettlementReachedAt()));
        content.put("acceptedOffer", "");
        content.put("acceptedOfferCompletionDate", formatDate(LocalDate.now()));
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
