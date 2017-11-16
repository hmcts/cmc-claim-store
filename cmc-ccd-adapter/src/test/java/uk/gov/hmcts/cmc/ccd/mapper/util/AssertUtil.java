package uk.gov.hmcts.cmc.ccd.mapper.util;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertUtil {

    private AssertUtil() {
    }

    public static void assertAddressEqualTo(Address address, uk.gov.hmcts.cmc.ccd.domain.Address ccdAddress) {
        assertThat(ccdAddress).isNotNull();
        assertThat(ccdAddress.getLine1()).isEqualTo(address.getLine1());
        assertThat(ccdAddress.getLine2()).isEqualTo(address.getLine2());
        assertThat(ccdAddress.getCity()).isEqualTo(address.getCity());
        assertThat(ccdAddress.getPostcode()).isEqualTo(address.getPostcode());
    }

    public static void assertContactDetailsEqualTo(ContactDetails contactDetails,
                                                   uk.gov.hmcts.cmc.ccd.domain.ContactDetails ccdContactDetails) {
        assertThat(contactDetails.getPhone().orElse(null)).isEqualTo(ccdContactDetails.getPhone());
        assertThat(contactDetails.getEmail().orElse(null)).isEqualTo(ccdContactDetails.getEmail());
        assertThat(contactDetails.getDxAddress().orElse(null)).isEqualTo(ccdContactDetails.getDxAddress());
    }
}
