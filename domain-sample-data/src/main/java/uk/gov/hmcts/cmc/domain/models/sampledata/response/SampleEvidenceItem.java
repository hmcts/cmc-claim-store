package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceType;

public class SampleEvidenceItem {
    private EvidenceType type = EvidenceType.PHOTO;
    private String description = "Photo of the claimant not lending me any money";

    public static SampleEvidenceItem builder() {
        return new SampleEvidenceItem();
    }

    public static EvidenceItem validDefaults() {
        return builder().build();
    }

    public SampleEvidenceItem withType(EvidenceType type) {
        this.type = type;
        return this;
    }

    public SampleEvidenceItem withDescription(final String description) {
        this.description = description;
        return this;
    }

    public EvidenceItem build() {
        return new EvidenceItem(type, description);
    }
}
