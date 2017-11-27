package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CompanyMapperTest {

    @Autowired
    private CompanyMapper companyMapper;

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Company company = SampleParty.builder().company();

        //when
        CCDCompany ccdCompany = companyMapper.to(company);

        //then
        assertThat(company).isEqualTo(ccdCompany);
    }

    @Test
    public void sholdMapCompanyFromCCD() {
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
        CCDCompany ccdCompany = CCDCompany.builder()
            .name("Abc Ltd")
            .address(ccdAddress)
            .mobilePhone("07987654321")
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .contactPerson("MR. Hyde")
            .build();

        //when
        Company company = companyMapper.from(ccdCompany);

        //then
        assertThat(company).isEqualTo(ccdCompany);
    }

}
