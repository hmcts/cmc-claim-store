package uk.gov.hmcts.cmc.domain.models.legalrep;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

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
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Representative that = (Representative) obj;

        return Objects.equals(this.organisationName, that.organisationName)
            && Objects.equals(this.organisationAddress, that.organisationAddress)
            && Objects.equals(this.organisationContactDetails, that.organisationContactDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            organisationName,
            organisationAddress,
            organisationContactDetails
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
