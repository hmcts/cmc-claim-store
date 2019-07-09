package uk.gov.hmcts.cmc.ccd-adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AddressMapperTest {

    @Autowired
    private AddressMapper mapper;

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
            .addressLine1("line1")
            .addressLine3("line2")
            .addressLine2("line3")
            .postTown("city")
            .postCode("postcode")
            .build();

        //when
        Address address = mapper.from(ccdAddress);

        //then
        assertThat(address).isEqualTo(ccdAddress);
    }
}
