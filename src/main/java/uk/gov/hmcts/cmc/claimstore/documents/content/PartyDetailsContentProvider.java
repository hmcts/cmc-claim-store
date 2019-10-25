package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class PartyDetailsContentProvider {

    /**
     * Returns party details content provided by the party.
     *
     * @param party      - party details
     * @param partyEmail - party email address
     * @return party details content
     */
    public PartyDetailsContent createContent(Party party, String partyEmail) {
        requireNonNull(party);

        return new PartyDetailsContent(
            PartyUtils.getType(party),
            party.getName(),
            false,
            PartyUtils.getBusinessName(party).orElse(null),
            PartyUtils.getContactPerson(party).orElse(null),
            party.getAddress(),
            false,
            party.getCorrespondenceAddress().orElse(null),
            party.getPhone().orElse(null),
            defendantDateOfBirth(party).orElse(null),
            partyEmail,
            null,
            null,
            false
        );
    }

    /**
     * Returns party details content provided by the party and tracks whether name or address has been changed.
     *
     * @param oppositeParty opposite party details (e.g. the claimant provides the defendant details or the defendant
     *                      provides claimant details)
     * @param ownParty      own party details (e.g. the claimant provides claimant details or the defendant provides
     *                      defendant details)
     * @param ownPartyEmail own party email address (e.g. the claimant provides claimant email or defendant provides
     *                      defendant email)
     * @return party details content
     */
    public PartyDetailsContent createContent(
        TheirDetails oppositeParty,
        Party ownParty,
        String ownPartyEmail,
        List<TimelineEvent> events,
        List<EvidenceContent> evidences
    ) {
        requireNonNull(oppositeParty);
        requireNonNull(ownParty);

        boolean nameAmended = !oppositeParty.getName().equals(ownParty.getName());
        boolean addressAmended = !oppositeParty.getAddress().equals(ownParty.getAddress());
        boolean phoneAmended = oppositeParty.getPhone().isPresent()
            && !(oppositeParty.getPhone().orElse("")
            .equalsIgnoreCase(ownParty.getPhone().orElse("")));

        return new PartyDetailsContent(
            PartyUtils.getType(ownParty),
            ownParty.getName(),
            nameAmended,
            PartyUtils.getBusinessName(ownParty).orElse(null),
            PartyUtils.getContactPerson(ownParty).orElse(null),
            ownParty.getAddress(),
            addressAmended,
            ownParty.getCorrespondenceAddress().orElse(null),
            ownParty.getPhone().orElse(null),
            defendantDateOfBirth(ownParty).orElse(null),
            ownPartyEmail,
            events,
            evidences,
            phoneAmended
        );
    }

    private Optional<String> defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return Optional.of(formatDate(((Individual) party).getDateOfBirth()));
        }
        return Optional.empty();
    }
}
