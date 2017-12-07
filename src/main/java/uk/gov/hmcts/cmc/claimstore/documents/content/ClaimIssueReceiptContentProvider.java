package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.PersonContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class ClaimIssueReceiptContentProvider {

    private final ClaimantContentProvider claimantContentProvider;
    private final PersonContentProvider personContentProvider;
    private final ClaimContentProvider claimContentProvider;

    @Autowired
    public ClaimIssueReceiptContentProvider(
        final ClaimantContentProvider claimantContentProvider,
        final PersonContentProvider personContentProvider,
        final ClaimContentProvider claimContentProvider) {
        this.claimantContentProvider = claimantContentProvider;
        this.personContentProvider = personContentProvider;
        this.claimContentProvider = claimContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);

        Map<String, Object> map = new HashMap<>();

        map.put("claimant", claimantContentProvider.createContent(
            claim.getClaimData().getClaimant(), claim.getSubmitterEmail())
        );

        TheirDetails defendant = claim.getClaimData().getDefendant();

        map.put("defendant", personContentProvider.createContent(
            PartyUtils.getType(defendant),
            defendant.getName(),
            defendant.getAddress(),
            null,
            defendant.getEmail().orElse(null),
            PartyUtils.getContactPerson(defendant).orElse(null),
            PartyUtils.getBusinessName(defendant).orElse(null))
        );
        map.put("claim", claimContentProvider.createContent(claim));

        return map;
    }

}
