package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import java.util.Optional;

public class EvidenceRowAssert extends CustomAssert<EvidenceRowAssert, EvidenceRow> {

    public EvidenceRowAssert(EvidenceRow actual) {
        super("EvidenceRow", actual, EvidenceRowAssert.class);
    }

    public EvidenceRowAssert isEqualTo(CCDEvidenceRow expected) {
        isNotNull();

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        compare("description",
            expected.getDescription(),
            actual.getDescription());

        return this;
    }

}
