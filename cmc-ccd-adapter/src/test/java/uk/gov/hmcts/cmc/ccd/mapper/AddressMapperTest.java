package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AddressMapperTest {


    Mapper<uk.gov.hmcts.cmc.ccd.domain.Address, Address> mapper = new AddressMapper();

    @Test
    public void shouldMapAddressToCCD() {
        //given
        Address address = SampleAddress.builder().build();

        //when
        uk.gov.hmcts.cmc.ccd.domain.Address ccdAddress = mapper.to(address);

        //then
        assertNotNull(ccdAddress);
        assertEquals(ccdAddress.getLine1(), address.getLine1());
        assertEquals(ccdAddress.getLine2(), address.getLine2());
        assertEquals(ccdAddress.getCity(), address.getCity());
        assertEquals(ccdAddress.getPostcode(), address.getPostcode());

    }

    @Test
    public void shouldMapAddressToCMC() {
        //given
        uk.gov.hmcts.cmc.ccd.domain.Address address = uk.gov.hmcts.cmc.ccd.domain.Address.builder()
            .line1("line1")
            .line2("line2")
            .city("city")
            .postcode("postcode")
            .build();

        //when
        Address cmcAddress = mapper.from(address);

        //then
        assertNotNull(cmcAddress);
        assertEquals(cmcAddress.getLine1(), address.getLine1());
        assertEquals(cmcAddress.getLine2(), address.getLine2());
        assertEquals(cmcAddress.getCity(), address.getCity());
        assertEquals(cmcAddress.getPostcode(), address.getPostcode());
    }
}
