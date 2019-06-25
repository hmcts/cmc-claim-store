package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ExpertReportMapperTest {

    @Autowired
    private ExpertReportMapper mapper;

    @Test
    public void shouldMapExpertRowMapperToCCD() {
        //given
        ExpertReport expertReport = ExpertReport
            .builder()
            .expertName("expert1")
            .expertReportDate(LocalDate.of(2050, 1, 1))
            .build();

        //when
        CCDCollectionElement<CCDExpertReport> ccdExpertReport = mapper.to(expertReport);

        //then
        assertThat(expertReport).isEqualTo(ccdExpertReport.getValue());
    }

    @Test
    public void shouldMapExpertRowMapperFromCCD() {
        //given
        CCDExpertReport ccdExpertReport = CCDExpertReport
            .builder()
            .expertName("expert1")
            .expertReportDate(LocalDate.of(2050, 1, 1))
            .build();

        //when
        ExpertReport expertReport = mapper.from(CCDCollectionElement.<CCDExpertReport>builder()
            .value(ccdExpertReport).build());

        //then
        assertThat(expertReport).isEqualTo(ccdExpertReport);
    }

}
