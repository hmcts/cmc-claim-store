package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Address;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;

@Component
public class HearingCourtMapper {

    public HearingCourt from(Court court) {
        return HearingCourt.builder()
            .name(court.getName())
            .address(mapHearingAddress(court.getAddress()))
            .build();
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
