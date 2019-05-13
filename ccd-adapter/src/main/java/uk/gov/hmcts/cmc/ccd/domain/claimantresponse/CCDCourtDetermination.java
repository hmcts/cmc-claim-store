package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;

@Builder
@Value
public class CCDCourtDetermination {
    private CCDPaymentIntention courtDecision;
    private CCDPaymentIntention courtIntention;
    private String rejectionReason;
    private String disposableIncome;
    private CCDDecisionType decisionType;
}
