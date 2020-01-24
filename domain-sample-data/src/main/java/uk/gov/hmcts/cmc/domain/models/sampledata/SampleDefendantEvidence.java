package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.Collections;
import java.util.List;

public class SampleDefendantEvidence {

    private List<EvidenceRow> rows = Collections.singletonList(SampleEvidenceRow.builder().build());

    private String comment = "More information";

    public static SampleDefendantEvidence builder() {
        return new SampleDefendantEvidence();
    }

    public static DefendantEvidence validDefaults() {
        return builder().build();
    }

    public SampleDefendantEvidence withRows(List<EvidenceRow> rows) {
        this.rows = rows;
        return this;
    }

    public SampleDefendantEvidence withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public DefendantEvidence build() {
        return new DefendantEvidence(rows, comment);
    }
}
