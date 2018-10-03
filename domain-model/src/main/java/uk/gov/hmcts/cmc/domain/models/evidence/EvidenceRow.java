package uk.gov.hmcts.cmc.domain.models.evidence;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
