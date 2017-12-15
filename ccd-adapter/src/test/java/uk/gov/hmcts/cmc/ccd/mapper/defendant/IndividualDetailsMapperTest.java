package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IndividualDetailsMapperTest {

    @Autowired
    private IndividualDetailsMapper individualDetailsMapper;

    @Test
    public void shouldMapIndividualDetailsToCCD() {
        //given
        IndividualDetails individualDetails = SampleTheirDetails.builder().individualDetails();

        //when
        CCDIndividual ccdIndividual = individualDetailsMapper.to(individualDetails);

        //then
        assertThat(individualDetails).isEqualTo(ccdIndividual);
    }

    @Test
    public void sholdMapIndividualDetailsFromCCD() {
        //given
        CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line1")
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
        CCDIndividual ccdIndividual = CCDIndividual.builder()
            .title("Mr.")
            .name("Individual")
            .email("my@email.com")
            .dateOfBirth("1950-01-01")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();

        //when
        IndividualDetails individualDetails = individualDetailsMapper.from(ccdIndividual);

        //then
        assertThat(individualDetails).isEqualTo(ccdIndividual);
    }

}
