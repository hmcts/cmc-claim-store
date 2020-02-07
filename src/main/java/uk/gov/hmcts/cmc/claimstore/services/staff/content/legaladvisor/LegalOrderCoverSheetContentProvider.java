package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getPartyNameFor;

@Component
public class LegalOrderCoverSheetContentProvider {
    private static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    private static final String PARTY_ADDRESS = "partyAddress";
    private static final String HMCTS_EMAIL = "hmctsEmail";
    private static final String PARTY_FULL_NAME = "partyFullName";

    private final StaffEmailProperties staffEmailProperties;

    public LegalOrderCoverSheetContentProvider(StaffEmailProperties staffEmailProperties) {
        this.staffEmailProperties = staffEmailProperties;
    }

    public Map<String, Object> createContentForClaimant(Claim claim) {
        return createContent(
            claim.getReferenceNumber(),
            getPartyNameFor(claim.getClaimData().getClaimant()),
            claim.getClaimData().getClaimant().getAddress()
        );
    }

    public Map<String, Object> createContentForDefendant(Claim claim) {
        return createContent(
            claim.getReferenceNumber(),
            claim.getClaimData().getDefendant().getName(),
            claim.getClaimData().getDefendant().getAddress()
        );
    }

    private Map<String, Object> createContent(String claimReferenceNumber, String partyFullName, Address partyAddress) {
        requireNonNull(claimReferenceNumber);
        requireNonNull(partyFullName);
        requireNonNull(partyAddress);

        Map<String, Object> content = new HashMap<>();
        content.put(PARTY_FULL_NAME, partyFullName);
        content.put(CLAIM_REFERENCE_NUMBER, claimReferenceNumber);
        content.put(PARTY_ADDRESS, partyAddress);
        content.put(HMCTS_EMAIL, staffEmailProperties.getRecipient());
        return content;
    }
}
