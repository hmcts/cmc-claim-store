package uk.gov.hmcts.cmc.claimstore.models.otherparty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.ServiceAddress;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;
import uk.gov.hmcts.cmc.claimstore.models.party.NamedParty;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

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

    @Valid
    private final ServiceAddress serviceAddress;

    @Email
    private final String email;

    @Valid
    public final Representative representative;

    public TheirDetails(
        final String name,
        final Address address,
        final ServiceAddress serviceAddress,
        final String email,
        final Representative representative
    ) {
        this.name = name;
        this.address = address;
        this.serviceAddress = serviceAddress;
        this.email = email;
        this.representative = representative;
    }

    @Override
    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Optional<ServiceAddress> getServiceAddress() {
        return Optional.ofNullable(serviceAddress);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<Representative> getRepresentative() {
        return Optional.ofNullable(representative);
    }

    @Override
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
            && Objects.equals(this.serviceAddress, that.serviceAddress)
            && Objects.equals(this.email, that.email)
            && Objects.equals(this.representative, that.representative);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, serviceAddress, email, representative);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
