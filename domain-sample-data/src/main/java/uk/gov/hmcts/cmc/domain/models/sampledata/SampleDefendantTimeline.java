package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.util.Collections;
import java.util.List;

public class SampleDefendantTimeline {

    private List<TimelineEvent> events = Collections.singletonList(SampleTimelineEvent.builder()
        .withCollectionId("3616a889-dd1e-496c-ac13-7dd3decc1225")
        .build());

    private String comment = "More information";

    public static SampleDefendantTimeline builder() {
        return new SampleDefendantTimeline();
    }

    public static DefendantTimeline validDefaults() {
        return builder().build();
    }

    public SampleDefendantTimeline withEvents(List<TimelineEvent> events) {
        this.events = events;
        return this;
    }

    public SampleDefendantTimeline withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public DefendantTimeline build() {
        return new DefendantTimeline(events, comment);
    }
}
