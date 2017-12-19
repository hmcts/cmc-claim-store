package uk.gov.hmcts.cmc.domain.models.response;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Evidence {
    @Valid
    @NotNull
    private final ImmutableList<EvidenceItem> rows;

    public Evidence(ImmutableList<EvidenceItem> rows) {
        this.rows = rows;
    }

    public ImmutableList<EvidenceItem> getRows() {
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
        Evidence that = (Evidence) other;
        return Objects.equals(rows, that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }
}
