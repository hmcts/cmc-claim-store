package uk.gov.hmcts.cmc.domain.models;

import java.util.Optional;
import javax.validation.constraints.Size;

public class EvidenceRow {

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
}
