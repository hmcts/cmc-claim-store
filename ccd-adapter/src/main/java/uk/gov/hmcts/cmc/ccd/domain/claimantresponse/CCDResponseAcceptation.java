package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseAcceptation extends CCDClaimantResponse {

    private CCDCourtDetermination courtDetermination;
    private CCDPaymentIntention claimantPaymentIntention;
    private CCDFormaliseOption formaliseOption;

    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.ACCEPTATION;
    }

    @Builder
    public CCDResponseAcceptation(
        BigDecimal amountPaid,
        LocalDateTime submittedOn,
        CCDPaymentIntention claimantPaymentIntention,
        CCDFormaliseOption formaliseOption,
        CCDCourtDetermination courtDetermination,
        CCDYesNoOption paymentReceived,
        CCDYesNoOption settleForAmount
    ) {
        super(amountPaid, submittedOn, paymentReceived, settleForAmount);
        this.claimantPaymentIntention = claimantPaymentIntention;
        this.formaliseOption = formaliseOption;
        this.courtDetermination = courtDetermination;
    }
}
