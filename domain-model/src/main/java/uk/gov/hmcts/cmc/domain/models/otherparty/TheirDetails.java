package uk.gov.hmcts.cmc.domain.models.otherparty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.Email;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.CollectionId;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.NamedParty;

import java.util.Optional;
import javax.swing.text.html.Option;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

/**
 * This class and its subtypes represent the data that a person provides about the other party.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = IndividualDetails.class, name = "individual"),
        @JsonSubTypes.Type(value = SoleTraderDetails.class, name = "soleTrader"),
        @JsonSubTypes.Type(value = CompanyDetails.class, name = "company"),
        @JsonSubTypes.Type(value = OrganisationDetails.class, name = "organisation")
    }
)
@EqualsAndHashCode(callSuper = true)
public abstract class TheirDetails extends CollectionId implements NamedParty {

    private final String name;

    @Valid
    @NotNull
    private final Address address;

    @Email(regexp = "\\S+")
    private final String email;

    @Valid
    private final Representative representative;

    private final Address claimantProvidedAddress;
    @Valid
    private final Address serviceAddress;

    @Size(max = 30, message = "may not be longer than {max} characters")
    private final String phone;

    public TheirDetails(
        String id,
        String name,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        Address claimantProvidedAddress,
        String phone
    ) {
        super(id);
        this.name = name;
        this.address = address;
        this.email = email;
        this.representative = representative;
        this.serviceAddress = serviceAddress;
        this.claimantProvidedAddress = claimantProvidedAddress;
        this.phone = phone;
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

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Address getclaimantProvidedAddress() { return claimantProvidedAddress; }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
