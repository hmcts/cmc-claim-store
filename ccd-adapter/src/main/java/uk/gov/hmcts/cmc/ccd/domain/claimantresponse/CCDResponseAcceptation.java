package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseAcceptation extends CCDClaimantResponse {

    private CCDPaymentIntention claimantPaymentIntention;
    private FormaliseOption formaliseOption;

    @Builder
    public CCDResponseAcceptation(
        BigDecimal amountPaid,
        LocalDateTime submittedOn,
        CCDPaymentIntention claimantPaymentIntention,
        FormaliseOption formaliseOption
    ) {
        super(amountPaid, submittedOn);
        this.claimantPaymentIntention = claimantPaymentIntention;
        this.formaliseOption = formaliseOption;
    }

    public ClaimantResponseType getClaimantResponseType() {
        return ClaimantResponseType.ACCEPTATION;
    }
}
