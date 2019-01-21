package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType;

public class SampleEvidenceRow {

    private EvidenceType evidenceType = EvidenceType.CORRESPONDENCE;
    private String collectionId;
    private String description = "description";

    public static SampleEvidenceRow builder() {
        return new SampleEvidenceRow();
    }

    public SampleEvidenceRow withEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
        return this;
    }

    public SampleEvidenceRow withDescription(String description) {
        this.description = description;
        return this;
    }

    public SampleEvidenceRow withCollectionId(String collectionId) {
        this.collectionId = collectionId;
        return this;
    }

    public EvidenceRow build() {
        return new EvidenceRow(collectionId, evidenceType, description);
    }
}
