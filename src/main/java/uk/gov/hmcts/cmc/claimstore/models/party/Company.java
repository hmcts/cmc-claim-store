package uk.gov.hmcts.cmc.claimstore.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Company extends Party {

    @NotBlank
    private final String contactPerson;

    public Company(
        String name,
        Address address,
        Address correspondenceAddress,
        String mobilePhone,
        Representative representative,
        String contactPerson
    ) {
        super(name, address, correspondenceAddress, mobilePhone, representative);
        this.contactPerson = contactPerson;
    }

    public String getContactPerson() {
        return contactPerson;
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
