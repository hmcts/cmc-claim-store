package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Component
public class ClaimantContentProvider {

    private final PersonContentProvider personContentProvider;

    @Autowired
    public ClaimantContentProvider(PersonContentProvider personContentProvider) {
        this.personContentProvider = personContentProvider;
    }

    public ClaimantContent createContent(Party claimant, String submitterEmail) {
        requireNonNull(claimant);
        requireNonBlank(submitterEmail);
        PersonContent personContent = personContentProvider.createContent(
            PartyUtils.getType(claimant),
            claimant.getName(),
            claimant.getAddress(),
            claimant.getCorrespondenceAddress().orElse(null),
            submitterEmail,
            PartyUtils.getContactPerson(claimant).orElse(null),
            PartyUtils.getBusinessName(claimant).orElse(null),
            claimant.getPhone().orElse(null),
            PartyUtils.claimantDateOfBirth(claimant).map(Formatting::formatDate).orElse(null)
        );
        return new ClaimantContent(
            personContent.getPartyType(),
            personContent.getFullName(),
            personContent.getAddress(),
            personContent.getCorrespondenceAddress(),
            submitterEmail,
            personContent.getContactPerson(),
            personContent.getBusinessName(),
            personContent.getPhoneNumber(),
            personContent.getDateOfBirth()
        );
    }
}
