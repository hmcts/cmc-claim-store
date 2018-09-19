package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDResponseRejection {
    private BigDecimal amountPaid;

    private boolean freeMediation;

    private String reason;
}
