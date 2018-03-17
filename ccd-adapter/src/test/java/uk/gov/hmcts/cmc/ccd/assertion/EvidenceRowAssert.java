package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.Objects;

public class EvidenceRowAssert extends AbstractAssert<EvidenceRowAssert, EvidenceRow> {

    public EvidenceRowAssert(EvidenceRow actual) {
        super(actual, EvidenceRowAssert.class);
    }

    public EvidenceRowAssert isEqualTo(CCDEvidenceRow ccdEvidenceRow) {
        isNotNull();

        if (!Objects.equals(actual.getType().name(), ccdEvidenceRow.getType().name())) {
            failWithMessage("Expected EvidenceRow.type to be <%s> but was <%s>",
                ccdEvidenceRow.getType().name(), actual.getType().name());
        }

        if (!Objects.equals(actual.getDescription().orElse(null), ccdEvidenceRow.getDescription())) {
            failWithMessage("Expected EvidenceRow.description to be <%s> but was <%s>",
                ccdEvidenceRow.getDescription(), actual.getDescription());
        }

        return this;
    }

}
