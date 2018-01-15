package uk.gov.hmcts.cmc.domain.models.otherparty;

import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class IndividualDetails extends TheirDetails {

    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    public IndividualDetails(
        String name,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        LocalDate dateOfBirth
    ) {
        super(name, address, email, representative, serviceAddress);
        this.dateOfBirth = dateOfBirth;
    }

    public Optional<LocalDate> getDateOfBirth() {
        return Optional.ofNullable(dateOfBirth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        IndividualDetails that = (IndividualDetails) obj;
        return Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), dateOfBirth);
    }
}
