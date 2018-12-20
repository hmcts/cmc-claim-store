package uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

import java.math.BigDecimal;

@Builder
@Value
public class CCDCourtDetermination {
    private CCDPaymentIntention courtDecision;
    private CCDPaymentIntention courtPaymentIntention;
    private String rejectionReason;
    private BigDecimal disposableIncome;
    private DecisionType decisionType;
}
