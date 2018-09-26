package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class Representative {

    @NotBlank
    @Size(max = 255, message = "must be at most {max} characters")
    private final String organisationName;

    @Valid
    @NotNull
    private final Address organisationAddress;

    @Valid
    private final ContactDetails organisationContactDetails;

    public Representative(
        String organisationName,
        Address organisationAddress,
        ContactDetails organisationContactDetails
    ) {
        this.organisationName = organisationName;
        this.organisationAddress = organisationAddress;
        this.organisationContactDetails = organisationContactDetails;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public Address getOrganisationAddress() {
        return organisationAddress;
    }

    public Optional<ContactDetails> getOrganisationContactDetails() {
        return Optional.ofNullable(organisationContactDetails);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
