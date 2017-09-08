package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.utils.PartyTypeContentProvider;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Component
public class SealedClaimContentProvider {

    private final ClaimantContentProvider claimantContentProvider;
    private final PersonContentProvider personContentProvider;
    private final ClaimContentProvider claimContentProvider;

    @Autowired
    public SealedClaimContentProvider(ClaimantContentProvider claimantContentProvider,
                                      PersonContentProvider personContentProvider,
                                      ClaimContentProvider claimContentProvider) {
        this.claimantContentProvider = claimantContentProvider;
        this.personContentProvider = personContentProvider;
        this.claimContentProvider = claimContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim, final String submitterEmail) {
        requireNonNull(claim);
        requireNonBlank(submitterEmail);

        Map<String, Object> map = new HashMap<>();

        map.put("claimant", claimantContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            submitterEmail)
        );

        TheirDetails defendant = claim.getClaimData().getDefendant();

        map.put("defendant", personContentProvider.createContent(
            PartyTypeContentProvider.getType(defendant),
            defendant.getName(),
            defendant.getAddress(),
            null,
            defendant.getEmail().orElse(null),
            PartyTypeContentProvider.getDefendantContactPerson(defendant).orElse(null),
            PartyTypeContentProvider.getDefendantBusinessName(defendant).orElse(null))
        );

        map.put("claim", claimContentProvider.createContent(claim));
        map.put("responseDeadline", formatDate(claim.getResponseDeadline()));

        return map;
    }
}
