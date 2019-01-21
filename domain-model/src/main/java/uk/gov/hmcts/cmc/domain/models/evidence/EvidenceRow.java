package uk.gov.hmcts.cmc.domain.models.evidence;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
@Getter
public class EvidenceRow extends CollectionId {

    @NotNull
    private final EvidenceType type;

    @Size(max = 99000)
    private final String description;

    @Builder
    public EvidenceRow(String id, EvidenceType type, String description) {
        super(id);
        this.type = type;
        this.description = description;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
