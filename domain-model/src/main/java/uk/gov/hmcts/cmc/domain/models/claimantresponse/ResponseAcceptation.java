package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;

import java.math.BigDecimal;

public class ResponseAcceptation extends ClaimantResponse {

    @Builder
    @JsonCreator
    public ResponseAcceptation(BigDecimal amountPaid) {
        super(amountPaid);
    }
}
