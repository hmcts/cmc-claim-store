package uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CompanyDetailsMapperTest {

    @Autowired
    private CompanyDetailsMapper companyDetailsMapper;

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        CompanyDetails companyDetails = SampleTheirDetails.builder().companyDetails();

        //when
        CCDCompany ccdCompany = companyDetailsMapper.to(companyDetails);

        //then
        assertThat(companyDetails).isEqualTo(ccdCompany);
    }

    @Test
    public void sholdMapCompanyFromCCD() {
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
        CCDCompany ccdCompany = CCDCompany.builder()
            .name("My Company")
            .address(ccdAddress)
            .phoneNumber("07987654321")
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .contactPerson("MR. Hyde")
            .build();

        //when
        CompanyDetails company = companyDetailsMapper.from(ccdCompany);

        //then
        assertThat(company).isEqualTo(ccdCompany);
    }

}
