package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class EvidenceItem {
    private final EvidenceType type;

    private final String description;

    public EvidenceItem(final EvidenceType type, final String description) {
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
        EvidenceItem evidenceItem = (EvidenceItem) other;
        return Objects.equals(type, evidenceItem.type)
            && Objects.equals(description, evidenceItem.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, description);
    }
}
