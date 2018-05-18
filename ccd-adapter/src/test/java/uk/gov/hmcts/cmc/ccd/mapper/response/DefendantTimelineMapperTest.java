package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantTimelineMapperTest {

    @Autowired
    private DefendantTimelineMapper mapper;

    @Test
    public void shouldMapDefendantTimelineToCCD() {
        //given
        DefendantTimeline timeline = new DefendantTimeline(
            asList(new TimelineEvent("Last Year", "Work done")), "More info");

        //when
        CCDDefendantTimeline ccdTimeline = mapper.to(timeline);

        //then
        assertThat(timeline).isEqualTo(ccdTimeline);

    }

    @Test
    public void shouldMapDefendantTimelineFromCCD() {
        //given
        CCDTimelineEvent event = CCDTimelineEvent.builder()
            .date("Last Month")
            .description("My description")
            .build();

        CCDDefendantTimeline ccdDefendantTimeline = CCDDefendantTimeline.builder()
            .events(asList(CCDCollectionElement.<CCDTimelineEvent>builder().value(event).build()))
            .comment("More info")
            .build();

        //when
        DefendantTimeline timeline = mapper.from(ccdDefendantTimeline);

        //then
        assertThat(timeline).isEqualTo(ccdDefendantTimeline);
    }
}
