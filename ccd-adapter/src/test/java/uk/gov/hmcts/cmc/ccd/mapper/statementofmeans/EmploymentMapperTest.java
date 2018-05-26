package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EmploymentMapperTest {

    @Autowired
    private EmploymentMapper mapper;

    @Test
    public void shouldMapEmploymentToCCD() {
        //given
        Employment employment = Employment.builder()
            .isEmployed(YesNoOption.YES)
            .employers(asList(Employer.builder().employerName("CMC").jobTitle("My sweet job").build()))
            .isSelfEmployed(YesNoOption.NO)
            .build();

        //when
        CCDEmployment ccdEmployment = mapper.to(employment);

        //then
        assertThat(employment).isEqualTo(ccdEmployment);

    }

    @Test
    public void shouldMapEmploymentFromCCD() {
        //given
        CCDEmployer ccdEmployer = CCDEmployer.builder()
            .jobTitle("A job")
            .employerName("A Company")
            .build();

        CCDEmployment ccdEmployment = CCDEmployment.builder()
            .employers(asList(CCDCollectionElement.<CCDEmployer>builder().value(ccdEmployer).build()))
            .isEmployed(CCDYesNoOption.YES)
            .isSelfEmployed(CCDYesNoOption.NO)
            .build();

        //when
        Employment employment = mapper.from(ccdEmployment);

        //then
        assertThat(employment).isEqualTo(ccdEmployment);
    }
}
