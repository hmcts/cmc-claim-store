package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans.EmployerMapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EmployerMapperTest {

    @Autowired
    private EmployerMapper mapper;

    @Test
    public void shouldMapEmployerToCCD() {
        //given
        Employer employer = Employer.builder()
            .name("CMC")
            .jobTitle("My sweet job")
            .build();

        //when
        CCDEmployer ccdEmployer = mapper.to(employer);

        //then
        assertThat(employer).isEqualTo(ccdEmployer);

    }

    @Test
    public void shouldMapEmployerFromCCD() {
        //given
        CCDEmployer ccdEmployer = CCDEmployer.builder()
            .jobTitle("My job")
            .name("CCD")
            .build();

        //when
        Employer employer = mapper.from(ccdEmployer);

        //then
        assertThat(employer).isEqualTo(ccdEmployer);
    }
}
