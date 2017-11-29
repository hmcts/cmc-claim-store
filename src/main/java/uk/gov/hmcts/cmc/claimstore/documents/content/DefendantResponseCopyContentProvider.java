package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ResponseData;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class DefendantResponseCopyContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimContentProvider claimContentProvider;

    public DefendantResponseCopyContentProvider(
        final PartyDetailsContentProvider partyDetailsContentProvider,
        final ClaimContentProvider claimContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimContentProvider = claimContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);
        ResponseData defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();

        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail()
        ));
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            claim.getClaimData().getClaimant(),
            claim.getDefendantEmail()
        ));
        content.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        content.put("responseDefence", defendantResponse.getDefence());
        content.put("claim", claimContentProvider.createContent(claim));

        return content;
    }
}
