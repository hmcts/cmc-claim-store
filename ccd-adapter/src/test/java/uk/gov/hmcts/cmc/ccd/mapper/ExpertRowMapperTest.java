package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ExpertRowMapperTest {

    @Autowired
    private ExpertRowMapper mapper;

    @Test
    public void shouldMapExpertRowMapperToCCD() {
        //given
        ExpertReportRow expertReportRow = ExpertReportRow
            .builder()
            .expertName("expert1")
            .expertReportDate(LocalDate.of(2050,1,1))
            .build();

        //when
        CCDCollectionElement<CCDExpertReportRow> ccdExpertReportRow = mapper.to(expertReportRow);

        //then
        assertThat(expertReportRow).isEqualTo(ccdExpertReportRow.getValue());
        assertThat(expertReportRow.getId()).isEqualTo(ccdExpertReportRow.getId());
    }

}
