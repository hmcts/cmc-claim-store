package uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SoleTraderDetailsMapperTest {

    @Autowired
    private SoleTraderDetailsMapper soleTraderDetailsMapper;

    @Test
    public void shouldMapSoleTraderDetailsToCCD() {
        //given
        SoleTraderDetails soleTraderDetails = SampleTheirDetails.builder().soleTraderDetails();

        //when
        CCDSoleTrader ccdSoleTrader = soleTraderDetailsMapper.to(soleTraderDetails);

        //then
        assertThat(soleTraderDetails).isEqualTo(ccdSoleTrader);
    }

    @Test
    public void sholdMapSoleTraderDetailsFromCCD() {
        //given
        CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line2")
            .line3("line3")
            .city("city")
            .postcode("postcode")
            .build();
        CCDContactDetails ccdContactDetails = CCDContactDetails.builder()
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
            .email("my@email.com")
            .businessName("My Trade")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();

        //when
        SoleTraderDetails soleTraderDetails = soleTraderDetailsMapper.from(ccdSoleTrader);

        //then
        assertThat(soleTraderDetails).isEqualTo(ccdSoleTrader);
    }

}
