package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DefendantEvidenceAssert extends AbstractAssert<DefendantEvidenceAssert, DefendantEvidence> {

    public DefendantEvidenceAssert(DefendantEvidence actual) {
        super(actual, DefendantEvidenceAssert.class);
    }

    public DefendantEvidenceAssert isEqualTo(CCDDefendantEvidence ccdEvidence) {
        isNotNull();

        if (!Objects.equals(actual.getRows().size(), ccdEvidence.getRows().size())) {
            failWithMessage("Expected DefendantEvidence.size to be <%s> but was <%s>",
                actual.getRows().size(), ccdEvidence.getRows().size());
        }

        if (!Objects.equals(actual.getComment().orElse(null), ccdEvidence.getComment())) {
            failWithMessage("Expected DefendantEvidence.comment to be <%s> but was <%s>",
                actual.getComment().orElse(null), ccdEvidence.getComment());
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
