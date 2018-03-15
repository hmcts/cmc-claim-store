package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;

import static java.util.Arrays.asList;

public class SampleEvidence {

    private List<EvidenceRow> rows = asList(SampleEvidenceRow.builder().build());

    public static SampleEvidence builder() {
        return new SampleEvidence();
    }

    public static Evidence validDefaults() {
        return builder().build();
    }

    public SampleEvidence withRows(List<EvidenceRow> rows) {
        this.rows = rows;
        return this;
    }

    public Evidence build() {
        return new Evidence(rows);
    }
}
