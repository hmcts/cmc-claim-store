package uk.gov.hmcts.cmc.domain.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class PartyContactDetails {
    @Valid
    @NotNull
    private final Address address;

    @Valid
    private final Address correspondenceAddress;

    @Valid
    private final String phoneNumber;

    public PartyContactDetails(
        Address address,
        Address correspondenceAddress,
        String phoneNumber
    ) {
        this.address = address;
        this.correspondenceAddress = correspondenceAddress;
        this.phoneNumber = phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyContactDetails that = (PartyContactDetails) o;
        return Objects.equals(address, that.address) &&
            Objects.equals(correspondenceAddress, that.correspondenceAddress) &&
            Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, correspondenceAddress, phoneNumber);
    }

    @Override
    public String toString() {
        return "PartyContactDetails{" +
            "address=" + address +
            ", correspondenceAddress=" + correspondenceAddress +
            ", phoneNumber='" + phoneNumber + '\'' +
            '}';
    }
}
