package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;

import static java.util.Objects.requireNonNull;

@Component
public class PersonContentProvider {

    public PersonContent createContent(String name, Address address, Address correspondenceAddress, String email) {
        requireNonNull(name);
        requireNonNull(address);

        return new PersonContent(
            name,
            address,
            correspondenceAddress,
            email);
    }

}
