package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Timeline {

    @Valid
    @Size(min = 1, max = 1000)
    @JsonProperty("rows")
    private final List<TimelineEvent> events;

    @JsonCreator
    public Timeline(List<TimelineEvent> events) {
        this.events = events;
    }

    public List<TimelineEvent> getEvents() {
        return events;
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
        return Objects.equals(events, timeline.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
