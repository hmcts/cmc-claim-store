package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.constraints.ValidAdmission;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ValidAdmission
public abstract class AdmissionResponse extends Response {
    private final PaymentOption paymentOption;

    @DateNotInThePast
    private final LocalDate paymentDate;

    @Valid
    private final RepaymentPlan repaymentPlan;

    @Valid
    private final StatementOfMeans statementOfMeans;

    public AdmissionResponse(
        ResponseType responseType,
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PaymentOption paymentOption,
        LocalDate paymentDate,
        RepaymentPlan repaymentPlan,
        StatementOfMeans statementOfMeans
    ) {
        super(responseType, freeMediation, moreTimeNeeded, defendant, statementOfTruth);
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        AdmissionResponse that = (AdmissionResponse) other;
        return super.equals(other)
            && paymentOption == that.paymentOption
            && Objects.equals(paymentDate, that.paymentDate)
            && Objects.equals(repaymentPlan, that.repaymentPlan)
            && Objects.equals(statementOfMeans, that.statementOfMeans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentOption, paymentDate, repaymentPlan, statementOfMeans);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
