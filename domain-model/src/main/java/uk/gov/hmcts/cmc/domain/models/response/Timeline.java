package uk.gov.hmcts.cmc.domain.models.response;

import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Timeline {

    @Valid
    @NotNull
    private final List<TimelineEvent> rows;

    public Timeline(final List<TimelineEvent> rows) {
        this.rows = rows;
    }

    public List<TimelineEvent> getRows() {
        return rows;
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
        return Objects.equals(rows, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }

}
