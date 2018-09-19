package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;

@Builder
@Value
public class CCDCourtDetermination {
    private CCDPaymentIntention courtCalculatedPaymentIntention;

    private String rejectionReason;
}
