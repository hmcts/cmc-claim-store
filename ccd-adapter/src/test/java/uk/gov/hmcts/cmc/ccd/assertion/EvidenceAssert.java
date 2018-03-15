package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class EvidenceAssert extends AbstractAssert<EvidenceAssert, Evidence> {

    public EvidenceAssert(Evidence actual) {
        super(actual, EvidenceAssert.class);
    }

    public EvidenceAssert isEqualTo(CCDEvidence ccdEvidence) {
        isNotNull();

        if (!Objects.equals(actual.getRows().size(), ccdEvidence.getRows().size())) {
            failWithMessage("Expected Evidence.size to be <%s> but was <%s>",
                actual.getRows().size(), ccdEvidence.getRows().size());
        }

        actual.getRows()
            .forEach(evidenceRow -> assertEvidenceRow(evidenceRow, ccdEvidence.getRows()));

        return this;
    }

    private void assertEvidenceRow(
        EvidenceRow actual,
        List<CCDCollectionElement<CCDEvidenceRow>> ccdEvidences
    ) {
        ccdEvidences.stream()
            .map(CCDCollectionElement::getValue)
            .filter(evidenceRow -> actual.getType().name().equals(evidenceRow.getType().name()))
            .findFirst()
            .ifPresent(evidenceRow -> assertThat(actual).isEqualTo(evidenceRow));
    }
}
