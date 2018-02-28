package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Timeline {

    private final List<TimelineEvent> timelineEvents;

    public Timeline(List<TimelineEvent> events) {
        this.timelineEvents = events;
    }

    public List<TimelineEvent> getTimelineEvents() {
        return timelineEvents;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Timeline timeline = (Timeline) other;
        return Objects.equals(timelineEvents, timeline.timelineEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timelineEvents);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
