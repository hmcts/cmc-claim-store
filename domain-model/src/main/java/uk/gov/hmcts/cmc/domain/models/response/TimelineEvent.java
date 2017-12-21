package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import java.util.Objects;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class TimelineEvent {

    @NotBlank
    @Size(max = 25)
    private final String date;

    @NotBlank
    @Size(max = 99000)
    private final String description;

    public TimelineEvent(final String date, final String description) {
        this.date = date;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TimelineEvent timelineEvent = (TimelineEvent) other;
        return Objects.equals(date, timelineEvent.date)
            && Objects.equals(description, timelineEvent.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
