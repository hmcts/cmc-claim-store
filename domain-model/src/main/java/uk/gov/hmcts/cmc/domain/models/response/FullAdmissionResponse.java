package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInThePast;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.FinancialDetails;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class FullAdmissionResponse extends Response {

    @NotNull
    private final DefenceType defenceType;

    @NotNull
    private final PaymentOption paymentOption;

    @JsonUnwrapped
    @DateNotInThePast
    private final LocalDate paymentDate;

    private final RepaymentPlan repaymentPlan;

    @NotNull
    private final FinancialDetails financialDetails;

    public FullAdmissionResponse(
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        DefenceType defenceType,
        PaymentOption paymentOption,
        LocalDate paymentDate,
        RepaymentPlan repaymentPlan,
        FinancialDetails financialDetails
    ) {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.defenceType = defenceType;
        this.paymentOption = paymentOption;
        this.paymentDate = paymentDate;
        this.repaymentPlan = repaymentPlan;
        this.financialDetails = financialDetails;
    }

    public DefenceType getDefenceType() {
        return defenceType;
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

    public FinancialDetails getFinancialDetails() {
        return financialDetails;
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
        FullAdmissionResponse that = (FullAdmissionResponse) other;
        return defenceType == that.defenceType
            && paymentOption == that.paymentOption
            && Objects.equals(paymentDate, that.paymentDate)
            && Objects.equals(repaymentPlan, that.repaymentPlan)
            && Objects.equals(financialDetails, that.financialDetails);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), defenceType, paymentOption, paymentDate, repaymentPlan, financialDetails);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
