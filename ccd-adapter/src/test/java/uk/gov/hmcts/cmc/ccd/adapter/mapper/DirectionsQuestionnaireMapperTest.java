package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.adapter.util.SampleCCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;

import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectionsQuestionnaireMapperTest {

    @Autowired
    private DirectionsQuestionnaireMapper mapper;

    @Test
    public void shouldMapDirectionsQuestionnaireMapperToCCD() {
        //given
        DirectionsQuestionnaire directionsQuestionnaire = SampleDirectionsQuestionnaire.builder().build();

        //when
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire = mapper.to(directionsQuestionnaire);

        //then
        assertThat(directionsQuestionnaire).isEqualTo(ccdDirectionsQuestionnaire);
    }

    @Test
    public void shouldMapDirectionsQuestionnaireMapperFromCCD() {
        //given
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire = SampleCCDDirectionsQuestionnaire.builder().build();

        //when
        DirectionsQuestionnaire directionsQuestionnaire = mapper.from(ccdDirectionsQuestionnaire);

        //then
        assertThat(directionsQuestionnaire).isEqualTo(ccdDirectionsQuestionnaire);
    }
}
