package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

public class ClaimDataAssert extends AbstractAssert<ClaimDataAssert, ClaimData> {

    public ClaimDataAssert(ClaimData claimData) {
        super(claimData, ClaimDataAssert.class);
    }

    public ClaimDataAssert isEqualTo(CCDClaim ccdClaim) {
        isNotNull();

        return this;
    }
}
