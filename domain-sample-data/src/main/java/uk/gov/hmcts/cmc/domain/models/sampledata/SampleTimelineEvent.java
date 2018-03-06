package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

public class SampleTimelineEvent {

    private String date = "Last Year";
    private String description = "signed a contract";

    public static SampleTimelineEvent builder() {
        return new SampleTimelineEvent();
    }

    public static TimelineEvent validDefaults() {
        return builder().build();
    }

    public SampleTimelineEvent withDate(String date) {
        this.date = date;
        return this;
    }

    public SampleTimelineEvent withDescription(String description) {
        this.description = description;
        return this;
    }

    public TimelineEvent build() {
        return new TimelineEvent(date, description);
    }
}
