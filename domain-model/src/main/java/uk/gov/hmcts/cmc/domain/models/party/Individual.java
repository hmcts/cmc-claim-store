package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual extends Party implements TitledParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    @JsonUnwrapped
    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    public Individual(
        final String name,
        final Address address,
        final Address correspondenceAddress,
        final String mobilePhone,
        final Representative representative,
        final String title,
        final LocalDate dateOfBirth
    ) {
        super(name, address, correspondenceAddress, mobilePhone, representative);
        this.title = title;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
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
            && Objects.equals(title, other.title)
            && Objects.equals(dateOfBirth, other.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, dateOfBirth);
    }

}
