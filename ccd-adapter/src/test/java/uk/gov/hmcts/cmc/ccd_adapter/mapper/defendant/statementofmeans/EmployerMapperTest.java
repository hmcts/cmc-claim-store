package uk.gov.hmcts.cmc.ccd-adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

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
        CCDCollectionElement<CCDEmployer> ccdEmployer = mapper.to(employer);

        //then
        assertThat(employer).isEqualTo(ccdEmployer.getValue());
        assertThat(employer.getId()).isEqualTo(ccdEmployer.getId());
    }

    @Test
    public void shouldMapEmployerFromCCD() {
        //given
        CCDEmployer ccdEmployer = CCDEmployer.builder()
            .jobTitle("My job")
            .employerName("CCD")
            .build();

        String collectionId = UUID.randomUUID().toString();

        //when
        Employer employer = mapper.from(CCDCollectionElement.<CCDEmployer>builder()
            .id(collectionId)
            .value(ccdEmployer).build());

        //then
        assertThat(employer).isEqualTo(ccdEmployer);
        assertThat(employer.getId()).isEqualTo(collectionId);
    }
}
