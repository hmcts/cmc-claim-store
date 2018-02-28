package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;


public class TimelineEvent {

    @JsonUnwrapped
    @NotNull
    @DateNotInTheFuture
    private final LocalDate eventDate;

    @NotBlank
    @Size(max = 99000)
    private final String description;

    public TimelineEvent(LocalDate eventDate, String description) {
        this.eventDate = eventDate;
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
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
        TimelineEvent that = (TimelineEvent) other;

        return Objects.equals(eventDate, that.eventDate)
            && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventDate, description);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
