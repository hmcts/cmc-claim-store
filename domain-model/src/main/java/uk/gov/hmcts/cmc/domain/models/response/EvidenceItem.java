package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;


public class EvidenceItem {
    @NotNull
    private final EvidenceType type;

    @NotBlank
    @Size(max = 99000)
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
        EvidenceItem that = (EvidenceItem) other;
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
