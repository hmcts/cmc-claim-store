package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmccase.models.Address;

import static java.util.Objects.requireNonNull;

@Component
public class PersonContentProvider {

    public PersonContent createContent(
        final String partyType,
        final String name,
        final Address address,
        final Address correspondenceAddress,
        final String email,
        final String contactPerson,
        final String businessName
    ) {
        requireNonNull(name);
        requireNonNull(address);

        return new PersonContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            email,
            contactPerson,
            businessName
        );
    }

}
