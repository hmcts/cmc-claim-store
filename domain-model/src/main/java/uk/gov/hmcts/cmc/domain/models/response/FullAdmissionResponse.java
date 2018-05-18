package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;

public class FullAdmissionResponse extends Response {

    @NotNull
    private final DefenceType defenceType;

    @NotNull
    private final PaymentOption paymentOption;

    @JsonUnwrapped
    @DateNotInThePast
    private final LocalDate paymentDate;

    public FullAdmissionResponse(
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        DefenceType defenceType,
        PaymentOption paymentOption,
        LocalDate paymentDate
    ) {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.defenceType = defenceType;
        this.paymentOption = paymentOption;
        this.paymentDate = paymentDate;
    }

}
