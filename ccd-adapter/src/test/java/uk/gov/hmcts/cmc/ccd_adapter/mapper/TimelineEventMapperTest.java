package uk.gov.hmcts.cmc.ccd-adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year").description("Work done").build();

        //when
        CCDCollectionElement<CCDTimelineEvent> ccdTimelineEvent = mapper.to(timelineEvent);

        //then
        assertThat(timelineEvent).isEqualTo(ccdTimelineEvent.getValue());
        assertThat(timelineEvent.getId()).isEqualTo(ccdTimelineEvent.getId());
    }

    @Test
    public void shouldTimelineEventFromCCD() {
        //given
        CCDTimelineEvent ccdTimelineEvent = CCDTimelineEvent.builder()
            .date("Last Month")
            .description("My description")
            .build();

        String collectionId = UUID.randomUUID().toString();

        //when
        TimelineEvent timelineEvent = mapper.from(CCDCollectionElement.<CCDTimelineEvent>builder()
            .id(collectionId)
            .value(ccdTimelineEvent)
            .build()
        );

        //then
        assertThat(timelineEvent).isEqualTo(ccdTimelineEvent);
        assertThat(timelineEvent.getId()).isEqualTo(collectionId);
    }
}
