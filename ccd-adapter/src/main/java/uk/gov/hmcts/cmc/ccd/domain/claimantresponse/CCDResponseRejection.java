package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseRejection extends CCDClaimantResponse {

    private CCDYesNoOption freeMediationOption;

    private String reason;

    @Builder
    public CCDResponseRejection(BigDecimal amountPaid,
                                LocalDateTime submittedOn,
                                 CCDYesNoOption freeMediationOption,
                                 String reason) {
        super(amountPaid,submittedOn);
        this.freeMediationOption = freeMediationOption;
        this.reason = reason;
    }

    @Override
    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.REJECTION;
    }
}
