package uk.gov.hmcts.cmc.domain.models.otherparty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.NamedParty;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

/**
 * This class and its subtypes represent the data that a person provides about the other party.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = IndividualDetails.class, name = "individual"),
        @JsonSubTypes.Type(value = SoleTraderDetails.class, name = "soleTrader"),
        @JsonSubTypes.Type(value = CompanyDetails.class, name = "company"),
        @JsonSubTypes.Type(value = OrganisationDetails.class, name = "organisation")
    }
)
public abstract class TheirDetails implements NamedParty {

    @NotBlank
    @Size(max = 255, message = "may not be longer than {max} characters")
    private final String name;

    @Valid
    @NotNull
    private final Address address;

    @Email(regexp = "\\S+")
    private final String email;

    @Valid
    private final Representative representative;

    @Valid
    private final Address serviceAddress;

    public TheirDetails(
        final String name,
        final Address address,
        final String email,
        final Representative representative,
        final Address serviceAddress
    ) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.representative = representative;
        this.serviceAddress = serviceAddress;
    }

    @Override
    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<Representative> getRepresentative() {
        return Optional.ofNullable(representative);
    }

    public Optional<Address> getServiceAddress() {
        return Optional.ofNullable(serviceAddress);
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

        TheirDetails that = (TheirDetails) obj;

        return Objects.equals(this.name, that.name)
            && Objects.equals(this.address, that.address)
            && Objects.equals(this.email, that.email)
            && Objects.equals(this.representative, that.representative)
            && Objects.equals(this.serviceAddress, that.serviceAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, email, representative, serviceAddress);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
