package uk.gov.hmcts.cmc.domain.models.otherparty;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

public class OrganisationDetails extends TheirDetails implements HasContactPerson {

    @Size(max = 35, message = "may not be longer than {max} characters")
    private final String contactPerson;
    private final String companiesHouseNumber;

    public OrganisationDetails(
        String name,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        String contactPerson,
        String companiesHouseNumber
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
