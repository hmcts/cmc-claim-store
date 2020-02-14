package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.constraints.PartySplitName;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.time.LocalDate;
import java.util.Optional;
import javax.validation.constraints.Size;

@PartySplitName
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class Individual extends Party implements TitledParty, SplitNamedParty {

    @JsonUnwrapped
    @AgeRangeValidator
    private final LocalDate dateOfBirth;
    @Size(max = 35)
    private final String title;

    @Size(max = 255, message = "must be at most {max} characters")
    private final String firstName;

    @Size(max = 255, message = "must be at most {max}")
    private final String lastName;

    @Builder
    public Individual(
        String id,
        String name,
        String title,
        String firstName,
        String lastName,
        Address address,
        Address correspondenceAddress,
        String phone,
        String mobilePhone,
        Representative representative,
        LocalDate dateOfBirth
    ) {
        super(id, name, address, correspondenceAddress, phone, mobilePhone, representative);
        this.dateOfBirth = dateOfBirth;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String getName() {
        if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)) {
            return PartyUtils.fullNameFrom(title, firstName, lastName);
        }
        return super.getName();
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
}
