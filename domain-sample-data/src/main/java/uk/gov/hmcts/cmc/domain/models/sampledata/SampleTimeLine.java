package uk.gov.hmcts.cmc.domain.models.sampledata;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.domain.models.response.Timeline;
import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;

public class SampleTimeLine {

    private ImmutableList<TimelineEvent> rows = ImmutableList.of(
        new TimelineEvent("20th May 2017", "something happened")
    );

    public SampleTimeLine withoutRows() {
        this.rows = ImmutableList.of();
        return this;
    }

    public SampleTimeLine withRows(ImmutableList<TimelineEvent> rows) {
        this.rows = rows;
        return this;
    }

    public static SampleTimeLine builder() {
        return new SampleTimeLine();
    }

    public static Timeline validDefaults() {
        return builder().build();
    }

    public Timeline build() {
        return new Timeline(rows);
    }

}
