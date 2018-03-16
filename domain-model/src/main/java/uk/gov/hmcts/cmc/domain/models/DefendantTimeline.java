package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class DefendantTimeline extends Timeline {

    @Size(max = 99000)
    private final String comment;

    public DefendantTimeline(List<TimelineEvent> events, String comment) {
        super(events);
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        DefendantTimeline that = (DefendantTimeline) other;
        return Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), comment);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
