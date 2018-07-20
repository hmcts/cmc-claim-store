package uk.gov.hmcts.cmc.domain.models.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.ValidAdmission;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ValidAdmission
@EqualsAndHashCode(callSuper = true)
public class FullAdmissionResponse extends Response {

    @NotNull
    private final PaymentOption paymentOption;

    @DateNotInThePast
    private final LocalDate paymentDate;

    @Valid
    private final RepaymentPlan repaymentPlan;

    @Valid
    private final StatementOfMeans statementOfMeans;

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
        super(FULL_ADMISSION, freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.paymentOption = paymentOption;
        this.paymentDate = paymentDate;
        this.repaymentPlan = repaymentPlan;
        this.statementOfMeans = statementOfMeans;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public Optional<LocalDate> getPaymentDate() {
        return Optional.ofNullable(paymentDate);
    }

    public Optional<RepaymentPlan> getRepaymentPlan() {
        return Optional.ofNullable(repaymentPlan);
    }

    public Optional<StatementOfMeans> getStatementOfMeans() {
        return Optional.ofNullable(statementOfMeans);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
