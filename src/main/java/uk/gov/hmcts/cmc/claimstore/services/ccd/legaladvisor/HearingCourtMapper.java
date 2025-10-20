package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Address;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import java.util.List;
import java.util.Optional;

@Component
public class HearingCourtMapper {

    public HearingCourt from(Court court) {
        return HearingCourt.builder()
            .name(court.getName())
            .address(mapHearingAddress(court.getAddress() != null
                ? court.getAddress() : fetchAddress(court.getAddresses())))
            .build();
    }

    private Address fetchAddress(List<Address> addresses) {
        Optional<Address> address = addresses.stream()
            .filter(a -> a.getType().equals("Write to us"))
            .findFirst();
        return address.orElse(addresses.get(0));
    }

    private CCDAddress mapHearingAddress(Address address) {
        CCDAddress.CCDAddressBuilder ccdAddressBuilder = CCDAddress.builder()
            .postTown(address.getTown())
            .postCode(address.getPostcode());

        try {
            ccdAddressBuilder.addressLine1(address.getAddressLines().get(0));
            ccdAddressBuilder.addressLine2(address.getAddressLines().get(1));
            ccdAddressBuilder.addressLine3(address.getAddressLines().get(2));
        } catch (IndexOutOfBoundsException exc) {
            //the address line out of bounds is going to be set as null, which is ok
        }
        return ccdAddressBuilder.build();
    }

}
