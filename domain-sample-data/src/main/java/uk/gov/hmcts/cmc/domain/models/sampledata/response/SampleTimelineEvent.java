package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;

public class SampleTimelineEvent {

    private String date = "20th May 2017";
    private String description = "Something happened";

    public static SampleTimelineEvent builder() {
        return new SampleTimelineEvent();
    }

    public static TimelineEvent validDefaults() {
        return builder().build();
    }

    public SampleTimelineEvent withDate(final String date) {
        this.date = date;
        return this;
    }

    public SampleTimelineEvent withDescription(final String description) {
        this.description = description;
        return this;
    }

    public TimelineEvent build() {
        return new TimelineEvent(date, description);
    }
}
