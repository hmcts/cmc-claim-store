package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;

public class ResponseAcceptation extends ClaimantResponse {

    @Builder
    @JsonCreator
    public ResponseAcceptation(BigDecimal amountPaid,
                               CourtDetermination courtDetermination,
                               PaymentIntention claimantPaymentIntention) {
        super(amountPaid, courtDetermination, claimantPaymentIntention);
    }
}
