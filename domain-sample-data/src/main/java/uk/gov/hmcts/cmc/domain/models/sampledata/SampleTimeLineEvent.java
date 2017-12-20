package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;

public class SampleTimeLineEvent {

    private String date = "I don't owe the amount";
    private String description = "Something happened";


    public static SampleTimeLineEvent builder() {
        return new SampleTimeLineEvent();
    }

    public static TimelineEvent validDefaults() {
        return builder().build();
    }

    public SampleTimeLineEvent withExplanation(final String description) {
        this.description = description;
        return this;
    }

    public SampleTimeLineEvent withDate(final String date) {
        this.date = date;
        return this;
    }

    public TimelineEvent build() {
        return new TimelineEvent(date, description);
    }
}
