package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow.EvidenceRowBuilder;

import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.CORRESPONDENCE;

public class SampleEvidenceRow {

    private SampleEvidenceRow() {
        super();
    }

    public static EvidenceRowBuilder builder() {
        return EvidenceRow.builder()
            .type(CORRESPONDENCE)
            .description("description");
    }
}
