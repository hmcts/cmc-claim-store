package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimeline;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TimelineMapperTest {

    @Autowired
    private TimelineMapper mapper;

    @Test
    public void shouldMapTimelineToCCD() {
        //given
        Timeline timeline = new Timeline(asList(new TimelineEvent("Last Year", "Work done")));

        //when
        CCDTimeline ccdTimeline = mapper.to(timeline);

        //then
        assertThat(timeline).isEqualTo(ccdTimeline);

    }

    @Test
    public void shouldTimelineFromCCD() {
        //given
        CCDTimelineEvent event = CCDTimelineEvent.builder()
            .date("Last Month")
            .description("My description")
            .build();

        CCDTimeline ccdTimeline = CCDTimeline.builder()
            .events(asList(CCDCollectionElement.<CCDTimelineEvent>builder().value(event).build()))
            .build();

        //when
        Timeline timeline = mapper.from(ccdTimeline);

        //then
        assertThat(timeline).isEqualTo(ccdTimeline);
    }
}
