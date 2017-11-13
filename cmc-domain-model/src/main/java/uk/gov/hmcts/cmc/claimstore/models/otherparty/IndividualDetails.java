package uk.gov.hmcts.cmc.claimstore.models.otherparty;

import uk.gov.hmcts.cmc.claimstore.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;
import uk.gov.hmcts.cmc.claimstore.models.party.TitledParty;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

public class IndividualDetails extends TheirDetails implements TitledParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    public IndividualDetails(
        final String name,
        final Address address,
        final String email,
        final Representative representative,
        final Address serviceAddress,
        final String title,
        final LocalDate dateOfBirth
    ) {
        super(name, address, email, representative, serviceAddress);
        this.title = title;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
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

        IndividualDetails that = (IndividualDetails) obj;
        return super.equals(that) && Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.title);
    }

}
