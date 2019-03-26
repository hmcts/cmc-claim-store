package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseRejection extends CCDClaimantResponse {

    private CCDYesNoOption freeMediationOption;
    private CCDTelephone mediationPhoneNumber;
    private String mediationContactPerson;
    private String reason;

    @Builder
    public CCDResponseRejection(
        BigDecimal amountPaid,
        LocalDateTime submittedOn,
        CCDYesNoOption freeMediationOption,
        CCDTelephone mediationPhoneNumber,
        String mediationContactPerson,
        String reason,
        CCDYesNoOption paymentReceived,
        CCDYesNoOption settleForAmount
    ) {
        super(amountPaid, submittedOn, paymentReceived, settleForAmount);
        this.freeMediationOption = freeMediationOption;
        this.mediationPhoneNumber = mediationPhoneNumber;
        this.mediationContactPerson = mediationContactPerson;
        this.reason = reason;
    }

    @Override
    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.REJECTION;
    }
}
