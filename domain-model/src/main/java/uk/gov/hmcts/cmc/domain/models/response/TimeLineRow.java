package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class TimeLineRow {
    private final String date;

    private final String description;

    public TimeLineRow(final String date, final String description) {
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
        TimeLineRow timeLineRow = (TimeLineRow) other;
        return Objects.equals(date, timeLineRow.date)
            && Objects.equals(description, timeLineRow.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description);
    }
}
