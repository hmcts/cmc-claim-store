package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.Timeline;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SampleTimeLine {

    private List<TimelineEvent> rows = asList(
        new TimelineEvent("20th May 2017", "something happened")
    );

    public SampleTimeLine clearRows() {
        this.rows = new ArrayList<>();
        return this;
    }

    public SampleTimeLine withRows(List<TimelineEvent> rows) {
        this.rows = rows;
        return this;
    }

    public SampleTimeLine addRow(TimelineEvent row) {
        rows.add(row);
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
