package uk.gov.hmcts.cmc.domain.models.response;

import org.hibernate.validator.constraints.NotBlank;
import java.util.Objects;
import javax.validation.constraints.Size;

public class TimelineEvent {

    @NotBlank
    @Size(max = 25)
    private final String date;

    @NotBlank
    @Size(max = 99000)
    private final String description;

    public TimelineEvent(String date, String description) {
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
        TimelineEvent that = (TimelineEvent) other;
        return Objects.equals(date, that.date)
            && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description);
    }
}
