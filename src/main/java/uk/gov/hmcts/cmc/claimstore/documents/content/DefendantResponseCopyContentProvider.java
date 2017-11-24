package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
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

    public DefendantResponseCopyContentProvider(final PartyDetailsContentProvider partyDetailsContentProvider) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);
        ResponseData defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("claimSubmittedOn", formatDate(claim.getCreatedAt()));
        content.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        content.put("claimantFullName", claim.getClaimData().getClaimant().getName());
        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail()
        ));
        content.put("responseDefence", defendantResponse.getDefence());

        return content;
    }



//    ToDO:
//    Create a claimant so that instances like claimant.businessName will work
}
