package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Collections;
import java.util.List;

public class SampleTimeline {

    private List<TimelineEvent> events = Collections.singletonList(SampleTimelineEvent.builder().build());

    public static SampleTimeline builder() {
        return new SampleTimeline();
    }

    public static Timeline validDefaults() {
        return builder().build();
    }

    public SampleTimeline withEvents(List<TimelineEvent> events) {
        this.events = events;
        return this;
    }

    public Timeline build() {
        return new Timeline(events);
    }
}
