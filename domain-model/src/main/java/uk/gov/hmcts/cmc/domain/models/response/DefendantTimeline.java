package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class DefendantTimeline {

    @Valid
    @Size(max = 1000)
    @JsonProperty("rows")
    private final List<TimelineEvent> events;

    @Size(max = 99000)
    private final String comment;

    public DefendantTimeline(List<TimelineEvent> events, String comment) {
        this.events = events;
        this.comment = comment;
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public List<TimelineEvent> getEvents() {
        return this.events == null ? Collections.emptyList() : this.events;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
