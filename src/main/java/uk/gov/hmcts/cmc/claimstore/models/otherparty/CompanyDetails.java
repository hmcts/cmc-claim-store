package uk.gov.hmcts.cmc.claimstore.models.otherparty;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;

public class CompanyDetails extends TheirDetails {

    private final String contactPerson;

    public CompanyDetails(
        String name,
        Address address,
        String email,
        Representative representative,
        String contactPerson
    ) {
        super(name, address, email, representative);
        this.contactPerson = contactPerson;
    }

    public Optional<String> getContactPerson() {
        return Optional.ofNullable(contactPerson);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CompanyDetails that = (CompanyDetails) obj;

        return super.equals(that)
            && Objects.equals(this.contactPerson, that.contactPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.contactPerson);
    }

}
