package uk.gov.hmcts.cmc.domain.models.sampledata;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.domain.models.response.Evidence;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceType;

public class SampleEvidence {

    private ImmutableList<EvidenceItem> rows = ImmutableList.of(
        new EvidenceItem(EvidenceType.CONTRACTS_AND_AGREEMENTS, "my evidence")
    );

    public SampleEvidence withoutRows() {
        this.rows = ImmutableList.of();
        return this;
    }

    public SampleEvidence withRows(ImmutableList<EvidenceItem> rows) {
        this.rows = rows;
        return this;
    }

    public SampleEvidence withRow(EvidenceItem row) {
        rows.add(row);
        return this;
    }

    public static SampleEvidence builder() {
        return new SampleEvidence();
    }

    public static Evidence validDefaults() {
        return builder().build();
    }

    public Evidence build() {
        return new Evidence(rows);
    }

}
