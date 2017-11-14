package uk.gov.hmcts.cmccase.models.otherparty;

import uk.gov.hmcts.cmccase.models.Address;
import uk.gov.hmcts.cmccase.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

public class OrganisationDetails extends TheirDetails {

    @Size(max = 35, message = "may not be longer than {max} characters")
    private final String contactPerson;
    private final String companiesHouseNumber;

    public OrganisationDetails(
        final String name,
        final Address address,
        final String email,
        final Representative representative,
        final Address serviceAddress,
        final String contactPerson,
        final String companiesHouseNumber
    ) {
        super(name, address, email, representative, serviceAddress);
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
