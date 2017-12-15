package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.response.Evidence;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceType;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SampleEvidence {

    private List<EvidenceItem> rows = asList(
        new EvidenceItem(EvidenceType.CONTRACTS_AND_AGREEMENTS, "cmy evidence")
    );

    public SampleEvidence clearRows() {
        this.rows = new ArrayList<>();
        return this;
    }

    public SampleEvidence withRows(List<EvidenceItem> rows) {
        this.rows = rows;
        return this;
    }

    public SampleEvidence addRow(EvidenceItem row) {
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
