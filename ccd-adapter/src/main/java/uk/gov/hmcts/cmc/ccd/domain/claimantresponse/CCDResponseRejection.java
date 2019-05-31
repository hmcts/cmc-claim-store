package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.directionsQuestionnaire.DirectionsQuestionnaire;

import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class CCDResponseRejection extends CCDClaimantResponse {

    private CCDYesNoOption freeMediationOption;
    private CCDTelephone mediationPhoneNumber;
    private String mediationContactPerson;
    private String reason;
    private DirectionsQuestionnaire directionsQuestionnaire;

    @Builder
    public CCDResponseRejection(
        String amountPaid,
        LocalDateTime submittedOn,
        CCDYesNoOption freeMediationOption,
        CCDTelephone mediationPhoneNumber,
        String mediationContactPerson,
        String reason,
        CCDYesNoOption paymentReceived,
        CCDYesNoOption settleForAmount,
        DirectionsQuestionnaire directionsQuestionnaire
    ) {
        super(amountPaid, submittedOn, paymentReceived, settleForAmount);
        this.freeMediationOption = freeMediationOption;
        this.mediationPhoneNumber = mediationPhoneNumber;
        this.mediationContactPerson = mediationContactPerson;
        this.reason = reason;
        this.directionsQuestionnaire = directionsQuestionnaire;
    }

    @Override
    public CCDClaimantResponseType getClaimantResponseType() {
        return CCDClaimantResponseType.REJECTION;
    }
}
