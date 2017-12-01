package uk.gov.hmcts.cmc.claimstore.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Claimant extends Party {

    public Claimant(
        final String name,
        final Address address,
        final Address correspondenceAddress,
        final String mobilePhone,
        final Representative representative
    ) {
        super(name, address, correspondenceAddress, mobilePhone, representative);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Claimant other = (Claimant) obj;

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

}
