package uk.gov.hmcts.cmc.claimstore.models.otherparty;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;

public class OrganisationDetails extends TheirDetails {

    private final String contactPerson;
    private final String companiesHouseNumber;

    public OrganisationDetails(final String name,
                               final Address address,
                               final String email,
                               final Representative representative,
                               final String contactPerson,
                               final String companiesHouseNumber) {
        super(name, address, email, representative);
        this.contactPerson = contactPerson;
        this.companiesHouseNumber = companiesHouseNumber;
    }

    public Optional<String> getContactPerson() {
        return Optional.ofNullable(contactPerson);
    }

    public Optional<String> getCompaniesHouseNumber() {
        return Optional.ofNullable(companiesHouseNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        OrganisationDetails that = (OrganisationDetails) obj;

        return super.equals(that)
            && Objects.equals(this.contactPerson, that.contactPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.contactPerson);
    }

}
