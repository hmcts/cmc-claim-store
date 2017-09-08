package uk.gov.hmcts.cmc.claimstore.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.constraints.MobilePhoneNumber;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

/**
 * This class and its subtypes represent the data that a person provides about themselves.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = Individual.class, name = "individual"),
        @JsonSubTypes.Type(value = SoleTrader.class, name = "soleTrader"),
        @JsonSubTypes.Type(value = Company.class, name = "company"),
        @JsonSubTypes.Type(value = Organisation.class, name = "organisation")
    }
)
public abstract class Party {

    @NotBlank
    @Size(max = 255, message = "may not be longer than {max} characters")
    private final String name;

    @Valid
    @NotNull
    private final Address address;

    @Valid
    private final Address correspondenceAddress;

    @MobilePhoneNumber
    private final String mobilePhone;

    @Valid
    private final Representative representative;

    public Party(final String name,
                 final Address address,
                 final Address correspondenceAddress,
                 final String mobilePhone,
                 final Representative representative) {
        this.name = name;
        this.address = address;
        this.correspondenceAddress = correspondenceAddress;
        this.mobilePhone = mobilePhone;
        this.representative = representative;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Optional<Address> getCorrespondenceAddress() {
        return Optional.ofNullable(correspondenceAddress);
    }

    public Optional<String> getMobilePhone() {
        return Optional.ofNullable(mobilePhone);
    }

    public Optional<Representative> getRepresentative() {
        return Optional.ofNullable(representative);
    }

    @SuppressWarnings("squid:S1067") // Number of conditional operators in boolean expression
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Party other = (Party) obj;

        return Objects.equals(name, other.name)
            && Objects.equals(address, other.address)
            && Objects.equals(correspondenceAddress, other.correspondenceAddress)
            && Objects.equals(mobilePhone, other.mobilePhone)
            && Objects.equals(representative, other.representative);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, correspondenceAddress, mobilePhone, representative);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
