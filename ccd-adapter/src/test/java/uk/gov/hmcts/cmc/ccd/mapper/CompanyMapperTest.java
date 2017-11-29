package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCompany;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDCompany;

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
        CCDCompany ccdCompany = getCCDCompany();

        //when
        Company company = companyMapper.from(ccdCompany);

        //then
        assertThat(company).isEqualTo(ccdCompany);
    }
}
