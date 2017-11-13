package uk.gov.hmcts.cmc.claimstore.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Organisation extends Party {

    private final String contactPerson;
    private final String companiesHouseNumber;

    public Organisation(
        final String name,
        final Address address,
        final Address correspondenceAddress,
        final String mobilePhone,
        final Representative representative,
        final String contactPerson,
        final String companiesHouseNumber
    ) {
        super(name, address, correspondenceAddress, mobilePhone, representative);
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

        Organisation other = (Organisation) obj;

        return super.equals(other) && Objects.equals(contactPerson, other.contactPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contactPerson);
    }

}
