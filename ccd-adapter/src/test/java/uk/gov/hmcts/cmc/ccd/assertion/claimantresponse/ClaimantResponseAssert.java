package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponseType.REJECTION;

public class ClaimantResponseAssert extends AbstractAssert<ClaimantResponseAssert, ClaimantResponse> {
    public ClaimantResponseAssert(ClaimantResponse response) {
        super(response, ClaimantResponseAssert.class);
    }

    public ClaimantResponseAssert isEqualTo(CCDClaimantResponse ccdResponse) {
        isNotNull();

        if (ccdResponse.getClaimantResponseType().equals(ACCEPTATION)) {
            assertThat((ResponseAcceptation) actual).isEqualTo(ccdResponse.getResponseAcceptation());
        }

        if (ccdResponse.getClaimantResponseType().equals(REJECTION)) {
            assertThat((ResponseRejection) actual).isEqualTo(ccdResponse.getResponseRejection());
        }

        return this;
    }
}
