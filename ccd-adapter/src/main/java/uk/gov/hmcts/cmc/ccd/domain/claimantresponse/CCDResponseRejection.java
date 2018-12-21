package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseRejection extends CCDClaimantResponse {

    private YesNoOption freeMediationOption;

    private String reason;

    @Builder
    public CCDResponseRejection(
        BigDecimal amountPaid,
        LocalDateTime submittedOn,
        YesNoOption freeMediationOption,
        String reason
    ) {
        super(amountPaid, submittedOn);
        this.freeMediationOption = freeMediationOption;
        this.reason = reason;
    }

    @Override
    public ClaimantResponseType getClaimantResponseType() {
        return ClaimantResponseType.REJECTION;
    }
}
