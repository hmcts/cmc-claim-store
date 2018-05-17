package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.util.List;

import static java.util.Arrays.asList;

public class SampleDefendantTimeline {

    private List<TimelineEvent> events = asList(SampleTimelineEvent.builder().build());

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
