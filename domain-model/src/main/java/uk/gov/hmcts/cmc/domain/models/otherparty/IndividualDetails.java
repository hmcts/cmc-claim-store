package uk.gov.hmcts.cmc.domain.models.otherparty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.TitledParty;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class IndividualDetails extends TheirDetails implements TitledParty {

    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    @NotBlank
    @Size(max = 255, message = "must be at most {max} characters")
    private final String firstName;

    @NotBlank
    @Size(max = 255, message = "must be at most {max} characters")
    private final String lastName;

    @Builder
    public IndividualDetails(
        String id,
        String name,
        String title,
        String firstName,
        String lastName,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        LocalDate dateOfBirth
    ) {
        super(id, name, address, email, representative, serviceAddress);
        this.dateOfBirth = dateOfBirth;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Optional<LocalDate> getDateOfBirth() {
        return Optional.ofNullable(dateOfBirth);
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
