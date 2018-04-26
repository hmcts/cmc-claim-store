package uk.gov.hmcts.cmc.domain.models.evidence;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Evidence {

    @Valid
    @Size(max = 1000)
    private final List<EvidenceRow> rows;

    @JsonCreator
    public Evidence(List<EvidenceRow> rows) {
        this.rows = rows;
    }

    public List<EvidenceRow> getRows() {
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
        Evidence evidence = (Evidence) other;
        return Objects.equals(rows, evidence.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
