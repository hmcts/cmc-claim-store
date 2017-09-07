package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;

import static java.util.Objects.requireNonNull;

@Component
public class DefendantDetailsContentProvider {

    public DefendantDetailsContent createContent(
        TheirDetails providedByClaimant,
        DefendantResponse defendantResponse
    ) {
        final Party defendant = defendantResponse.getResponse().getDefendant();
        requireNonNull(providedByClaimant);
        requireNonNull(defendant);
        requireNonNull(defendantResponse);
        return new DefendantDetailsContent(
            providedByClaimant,
            defendantResponse,
            defendant
        );
    }
}
