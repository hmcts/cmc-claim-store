package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.mapper.util.AssertUtil.assertAddressEqualTo;


public class AddressMapperTest {


    AddressMapper mapper = new AddressMapper();

    @Test
    public void shouldMapAddressToCCD() {
        //given
        Address address = SampleAddress.builder().build();

        //when
        uk.gov.hmcts.cmc.ccd.domain.Address ccdAddress = mapper.to(address);

        //then
        assertThat(ccdAddress).isNotNull();
        assertAddressEqualTo(address, ccdAddress);

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
        assertThat(cmcAddress).isNotNull();
        assertAddressEqualTo(cmcAddress, address);
    }
}
