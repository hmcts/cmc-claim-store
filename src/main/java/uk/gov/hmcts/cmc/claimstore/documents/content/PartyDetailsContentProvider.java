package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class PartyDetailsContentProvider {

    /**
     * Returns party details content provided by defendant and tracks whether name or address has been changed.
     *
     * @param providedByClaimant other party details
     * @param providedByDefendant party details
     * @param partyEmail party email address
     * @return content
     */
    public PartyDetailsContent createContent(
        final TheirDetails providedByClaimant,
        final Party providedByDefendant,
        final String partyEmail
    ) {
        requireNonNull(providedByClaimant);
        requireNonNull(providedByDefendant);

        boolean nameAmended = !providedByClaimant.getName().equals(providedByDefendant.getName());
        boolean addressAmended = !providedByClaimant.getAddress().equals(providedByDefendant.getAddress());

        return new PartyDetailsContent(
            PartyUtils.getType(providedByDefendant),
            providedByDefendant.getName(),
            nameAmended,
            PartyUtils.getBusinessName(providedByDefendant).orElse(null),
            PartyUtils.getContactPerson(providedByDefendant).orElse(null),
            providedByDefendant.getAddress(),
            addressAmended,
            providedByDefendant.getCorrespondenceAddress().orElse(null),
            defendantDateOfBirth(providedByDefendant).orElse(null),
            partyEmail
        );
    }

    /**
     * Returns party details content out of the party instance.
     *
     * @param party - party details
     * @param partyEmail - party email address
     * @return content
     */
    public PartyDetailsContent createContent(Party party, String partyEmail) {
        requireNonNull(party);
        requireNonNull(partyEmail);

        return new PartyDetailsContent(
            PartyUtils.getType(party),
            party.getName(),
            false,
            PartyUtils.getBusinessName(party).orElse(null),
            PartyUtils.getContactPerson(party).orElse(null),
            party.getAddress(),
            false, party.getCorrespondenceAddress().orElse(null),
            defendantDateOfBirth(party).orElse(null),
            partyEmail
        );
    }

    private Optional<String> defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return Optional.of(formatDate(((Individual) party).getDateOfBirth()));
        }
        return Optional.empty();
    }
}
