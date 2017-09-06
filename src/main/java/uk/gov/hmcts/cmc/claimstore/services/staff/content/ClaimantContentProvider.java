package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;

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

        PersonContent personContent = personContentProvider.createContent(claimant.getName(), claimant.getAddress(),
            claimant.getCorrespondenceAddress().orElse(null),
            submitterEmail);

        return new ClaimantContent(personContent.getFullName(), personContent.getAddress(),
            personContent.getCorrespondenceAddress(), submitterEmail);
    }

}
