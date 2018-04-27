package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual extends Party {

    @JsonUnwrapped
    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    public Individual(
        String name,
        Address address,
        Address correspondenceAddress,
        String phoneNumber,
        Representative representative,
        LocalDate dateOfBirth
    ) {
        super(name, address, correspondenceAddress, phoneNumber, representative);
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Individual other = (Individual) obj;

        return super.equals(other)
            && Objects.equals(dateOfBirth, other.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dateOfBirth);
    }

}
