package uk.gov.hmcts.cmc.claimstore.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Company extends Party {

    private final String contactPerson;

    public Company(
        final String name,
        final Address address,
        final Address correspondenceAddress,
        final String mobilePhone,
        final Representative representative,
        final String contactPerson
    ) {
        super(name, address, correspondenceAddress, mobilePhone, representative);
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

        Company other = (Company) obj;

        return super.equals(other) && Objects.equals(contactPerson, other.contactPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contactPerson);
    }

}
