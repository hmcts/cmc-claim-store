package uk.gov.hmcts.cmc.domain.models.evidence;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class EvidenceRow {

    @NotNull
    private final EvidenceType type;

    @Size(max = 99000)
    private final String description;

    public EvidenceRow(EvidenceType type, String description) {
        this.type = type;
        this.description = description;
    }

    public EvidenceType getType() {
        return type;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        EvidenceRow that = (EvidenceRow) other;

        return Objects.equals(type, that.type)
            && Objects.equals(description, that.description);
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
