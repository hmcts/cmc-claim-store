package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;


public class AddressMapperTest {


    AddressMapper mapper = new AddressMapper();

    @Test
    public void shouldMapAddressToCCD() {
        //given
        Address address = SampleAddress.builder().build();

        //when
        CCDAddress ccdAddress = mapper.to(address);

        //then
        assertThat(ccdAddress).isEqualTo(address);

    }

    @Test
    public void shouldMapAddressToCMC() {
        //given
        CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line2")
            .city("city")
            .postcode("postcode")
            .build();

        //when
        Address address = mapper.from(ccdAddress);

        //then
        assertThat(address).isEqualTo(ccdAddress);
    }
}
