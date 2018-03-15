package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType;

import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.CORRESPONDENCE;

public class SampleEvidenceRow {

    private EvidenceType type = CORRESPONDENCE;
    private String description = "description";

    public static SampleEvidenceRow builder() {
        return new SampleEvidenceRow();
    }

    public static EvidenceRow validDefaults() {
        return builder().build();
    }

    public SampleEvidenceRow withType(EvidenceType type) {
        this.type = type;
        return this;
    }

    public SampleEvidenceRow withDescription(String description) {
        this.description = description;
        return this;
    }

    public EvidenceRow build() {
        return new EvidenceRow(type, description);
    }
}
