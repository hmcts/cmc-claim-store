package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

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

        boolean nameAmended = !providedByClaimant.getName().equals(defendant.getName());
        String fullName = nameAmended ? defendant.getName() : providedByClaimant.getName();

        boolean addressAmended = !providedByClaimant.getAddress()
            .equals(defendant.getAddress());
        Address address = addressAmended ? defendant.getAddress() : providedByClaimant.getAddress();

        return new DefendantDetailsContent(
            fullName,
            nameAmended,
            address,
            defendantResponse.getResponse().getDefendant().getCorrespondenceAddress().orElse(null),
            addressAmended,
            defendantDateOfBirth(defendant),
            defendantResponse.getDefendantEmail()
        );
    }

    private String defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return formatDate(((Individual) party).getDateOfBirth());
        } else {
            return "Not available";
        }
    }

}
