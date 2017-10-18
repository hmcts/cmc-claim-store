package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

public class AmountContent {

    private String totalAmount;
    private String interestAmount;
    private String interestDailyAmount;
    private String feeAmount;
    private String paidAmount;
    private String remainingAmount;

    public AmountContent(
        String totalAmount,
        String interestAmount,
        String interestDailyAmount,
        String feeAmount,
        String paidAmount,
        String remainingAmount
    ) {
        this.totalAmount = totalAmount;
        this.interestAmount = interestAmount;
        this.interestDailyAmount = interestDailyAmount;
        this.feeAmount = feeAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getInterestAmount() {
        return interestAmount;
    }

    public String getInterestDailyAmount() {
        return interestDailyAmount;
    }

    public String getFeeAmount() {
        return feeAmount;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public String getRemainingAmount() {
        return remainingAmount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
