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
            .id("d839f2f0-025f-4ee9-9a98-16bbe6ab3b35")
            .type(CORRESPONDENCE)
            .description("description");
    }
}
