package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import static java.util.Objects.requireNonNull;

@Component
public class PartyDetailsContentProvider {

    public PartyDetailsContent createContent(
        final TheirDetails providedByClaimant,
        final Party party,
        final String defendantEmail
    ) {
        requireNonNull(providedByClaimant);
        requireNonNull(party);
        return new PartyDetailsContent(
            providedByClaimant,
            party,
            defendantEmail
        );
    }
}
