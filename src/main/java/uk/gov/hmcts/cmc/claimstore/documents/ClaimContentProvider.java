package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.PersonContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class ClaimContentProvider {

    private final ClaimantContentProvider claimantContentProvider;
    private final PersonContentProvider personContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;

    @Autowired
    public ClaimContentProvider(
        ClaimantContentProvider claimantContentProvider,
        PersonContentProvider personContentProvider,
        ClaimDataContentProvider claimDataContentProvider
    ) {
        this.claimantContentProvider = claimantContentProvider;
        this.personContentProvider = personContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Map<String, Object> map = new HashMap<>();

        map.put("claimant", claimantContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail())
        );

        TheirDetails defendant = claim.getClaimData().getDefendant();

        map.put("defendant", personContentProvider.createContent(
            PartyUtils.getType(defendant),
            defendant.getName(),
            defendant.getAddress(),
            null,
            defendant.getEmail().orElse(null),
            PartyUtils.getContactPerson(defendant).orElse(null),
            PartyUtils.getBusinessName(defendant).orElse(null),
            null,
            null)
        );

        map.put("claim", claimDataContentProvider.createContent(claim));
        map.put("responseDeadline", formatDate(claim.getResponseDeadline()));

        return map;
    }
}
