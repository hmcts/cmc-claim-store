package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;


@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class EvidenceItem {
    private final EvidenceType type;
    private final String description;

    public EvidenceItem(EvidenceType type, String description) {
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
