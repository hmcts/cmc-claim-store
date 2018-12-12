package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;

import java.math.BigDecimal;

@Value
@Builder
public class CCDResponseAcceptation extends CCDClaimantResponse {

    private BigDecimal amountPaid;
    private CCDCourtDetermination courtDetermination;
    private CCDPaymentIntention claimantPaymentIntention;
    private CCDFormaliseOption formaliseOption;

    @JsonProperty("claimantResponseType")
    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.ACCEPTATION;
    }
}
