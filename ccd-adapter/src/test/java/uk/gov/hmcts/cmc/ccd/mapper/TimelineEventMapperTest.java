package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TimelineEventMapperTest {

    @Autowired
    private TimelineEventMapper mapper;

    @Test
    public void shouldMapTimelineEventToCCD() {
        //given
        TimelineEvent timelineEvent = new TimelineEvent("Last Year", "Work done");

        //when
        CCDTimelineEvent ccdTimelineEvent = mapper.to(timelineEvent);

        //then
        assertThat(timelineEvent).isEqualTo(ccdTimelineEvent);

    }

    @Test
    public void shouldTimelineEventFromCCD() {
        //given
        CCDTimelineEvent ccdTimelineEvent = CCDTimelineEvent.builder()
            .date("Last Month")
            .description("My description")
            .build();

        //when
        TimelineEvent timelineEvent = mapper.from(ccdTimelineEvent);

        //then
        assertThat(timelineEvent).isEqualTo(ccdTimelineEvent);
    }
}
