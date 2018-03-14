package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDEvidence;
import uk.gov.hmcts.cmc.domain.models.Evidence;

import java.util.Objects;

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

        return this;
    }

}
