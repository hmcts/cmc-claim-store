package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

public class SampleTimelineEvent {

    private String date = "Last Year";
    private String description = "signed a contract";
    private String collectionId = "359fda9d-e5fd-4d6e-9525-238642d0157d";

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

    public SampleTimelineEvent withCollectionId(String collectionId) {
        this.collectionId = collectionId;
        return this;
    }

    public TimelineEvent build() {
        return new TimelineEvent(collectionId, date, description);
    }
}
