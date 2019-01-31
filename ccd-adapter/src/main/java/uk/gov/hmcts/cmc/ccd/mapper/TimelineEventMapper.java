package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

@Component
public class TimelineEventMapper {

    public CCDCollectionElement<CCDTimelineEvent> to(TimelineEvent timelineEvent) {
        if (timelineEvent == null) {
            return null;
        }

        CCDTimelineEvent.CCDTimelineEventBuilder builder = CCDTimelineEvent.builder();

        return CCDCollectionElement.<CCDTimelineEvent>builder()
            .value(builder.date(timelineEvent.getDate()).description(timelineEvent.getDescription()).build())
            .id(timelineEvent.getId())
            .build();
    }

    public TimelineEvent from(CCDCollectionElement<CCDTimelineEvent> ccdTimelineEvent) {
        return TimelineEvent.builder()
            .id(ccdTimelineEvent.getId())
            .description(ccdTimelineEvent.getValue().getDescription())
            .eventDate(ccdTimelineEvent.getValue().getDate())
            .build();
    }
}
