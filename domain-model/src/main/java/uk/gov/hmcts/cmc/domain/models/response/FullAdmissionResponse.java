package uk.gov.hmcts.cmc.domain.models.response;

import lombok.Builder;
import uk.gov.hmcts.cmc.domain.constraints.ValidFullAdmission;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;

@ValidFullAdmission
public class FullAdmissionResponse extends AdmissionResponse {
    @Builder
    public FullAdmissionResponse(
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PaymentOption paymentOption,
        LocalDate paymentDate,
        RepaymentPlan repaymentPlan,
        StatementOfMeans statementOfMeans
    ) {
        super(FULL_ADMISSION, freeMediation, moreTimeNeeded, defendant, statementOfTruth,
            paymentOption, paymentDate, repaymentPlan, statementOfMeans);
    }
}
