package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class EvidenceRow {
    private final EvidenceType type;

    private final String description;

    public EvidenceRow(final EvidenceType type, final String description) {
        this.type = type;
        this.description = description;
    }

    public EvidenceType getType() {
        return type;
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
        EvidenceRow evidenceRow = (EvidenceRow) other;
        return Objects.equals(type, evidenceRow.type)
            && Objects.equals(description, evidenceRow.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, description);
    }
}
