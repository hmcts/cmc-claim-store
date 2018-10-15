package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;

import java.math.BigDecimal;

@Builder
@Value
public class CCDCourtDetermination {
    private CCDPaymentIntention courtDecision;
    private CCDPaymentIntention courtPaymentIntention;
    private String rejectionReason;
    private BigDecimal disposableIncome;
}
