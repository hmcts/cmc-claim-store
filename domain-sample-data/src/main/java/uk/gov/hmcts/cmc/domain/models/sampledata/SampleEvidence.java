package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SampleEvidence {

    private List<EvidenceRow> rows = singletonList(SampleEvidenceRow.builder().build());

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
