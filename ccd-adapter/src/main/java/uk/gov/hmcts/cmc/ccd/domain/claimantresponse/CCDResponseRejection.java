package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;

import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseRejection extends CCDClaimantResponse {

    private CCDYesNoOption freeMediationOption;
    private CCDTelephone mediationPhoneNumber;
    private String mediationContactPerson;
    private String noMediationReason;
    private String reason;
    private CCDDirectionsQuestionnaire directionsQuestionnaire;

    @Builder
    public CCDResponseRejection(
        String amountPaid,
        LocalDateTime submittedOn,
        CCDYesNoOption freeMediationOption,
        CCDTelephone mediationPhoneNumber,
        String mediationContactPerson,
        String noMediationReason,
        String reason,
        CCDYesNoOption paymentReceived,
        CCDYesNoOption settleForAmount,
        CCDDirectionsQuestionnaire directionsQuestionnaire
    ) {
        super(amountPaid, submittedOn, paymentReceived, settleForAmount);
        this.freeMediationOption = freeMediationOption;
        this.mediationPhoneNumber = mediationPhoneNumber;
        this.mediationContactPerson = mediationContactPerson;
        this.noMediationReason = noMediationReason;
        this.reason = reason;
        this.directionsQuestionnaire = directionsQuestionnaire;
    }

    @Override
    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.REJECTION;
    }
}
