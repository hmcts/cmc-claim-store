package uk.gov.hmcts.cmc.domain.models.response;

import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Timeline {

    @Valid
    @NotNull
    private final List<TimeLineRow> rows;

    public Timeline(final List<TimeLineRow> rows) {
        this.rows = rows;
    }

    public List<TimeLineRow> getRows() {
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
        TimeLineRow that = (TimeLineRow) other;
        return Objects.equals(rows, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }

}
