package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.ccd.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SoleTraderMapperTest {

    @Autowired
    private SoleTraderMapper soleTraderMapper;

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        SoleTrader soleTrader = SampleParty.builder().soleTrader();

        //when
        CCDSoleTrader ccdSoleTrader = soleTraderMapper.to(soleTrader);

        //then
        assertThat(soleTrader).isEqualTo(ccdSoleTrader);
    }

    @Test
    public void sholdMapSoleTraderFromCCD() {
        //given
        final CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line1")
            .city("city")
            .postcode("postcode")
            .build();
        final CCDContactDetails ccdContactDetails = CCDContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
        CCDRepresentative ccdRepresentative = CCDRepresentative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(ccdContactDetails)
            .organisationAddress(ccdAddress)
            .build();
        CCDSoleTrader ccdSoleTrader = CCDSoleTrader.builder()
            .title("Mr.")
            .name("Individual")
            .mobilePhone("07987654321")
            .businessName("My Trade")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();

        //when
        SoleTrader soleTrader = soleTraderMapper.from(ccdSoleTrader);

        //then
        assertThat(soleTrader).isEqualTo(ccdSoleTrader);
    }

}
