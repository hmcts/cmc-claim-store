package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class DefendantResponseCopyContentProvider {

    private final DefendantDetailsContentProvider defendantDetailsContentProvider;

    public DefendantResponseCopyContentProvider(final DefendantDetailsContentProvider defendantDetailsContentProvider) {
        this.defendantDetailsContentProvider = defendantDetailsContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim, final DefendantResponse response) {
        requireNonNull(claim);
        requireNonNull(response);

        Map<String, Object> content = new HashMap<>();
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("claimSubmittedOn", formatDate(claim.getCreatedAt()));
        content.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        content.put("claimantFullName", claim.getClaimData().getClaimant().getName());
        content.put("defendant", defendantDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            response
        ));
        content.put("responseDefence", response.getResponse().getDefence());

        return content;
    }
}
