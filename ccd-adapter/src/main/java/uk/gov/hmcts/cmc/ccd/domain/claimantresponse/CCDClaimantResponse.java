package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "claimantResponseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CCDResponseAcceptation.class, name = "ACCEPTATION"),
    @JsonSubTypes.Type(value = CCDResponseRejection.class, name = "REJECTION")
})
@Getter
@EqualsAndHashCode
public abstract class CCDClaimantResponse {
    private final String amountPaid;
    private final LocalDateTime submittedOn;
    private final CCDYesNoOption paymentReceived;
    private final CCDYesNoOption settleForAmount;

    public CCDClaimantResponse(
        String amountPaid,
        LocalDateTime submittedOn,
        CCDYesNoOption paymentReceived,
        CCDYesNoOption settleForAmount
    ) {
        this.amountPaid = amountPaid;
        this.submittedOn = submittedOn;
        this.paymentReceived = paymentReceived;
        this.settleForAmount = settleForAmount;
    }

    public abstract CCDClaimantResponseType getClaimantResponseType();

}
