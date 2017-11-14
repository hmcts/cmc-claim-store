package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;

import static uk.gov.hmcts.cmccase.utils.ToStringStyle.ourStyle;

public class AmountContent {

    private String totalAmount;
    private InterestContent interest;
    private String feeAmount;
    private String paidAmount;
    private String remainingAmount;

    public AmountContent(
        String totalAmount,
        InterestContent interest,
        String feeAmount,
        String paidAmount,
        String remainingAmount
    ) {
        this.totalAmount = totalAmount;
        this.interest = interest;
        this.feeAmount = feeAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
    }

    public InterestContent getInterest() {
        return interest;
    }

    public String getTotalAmount() {
        return totalAmount;
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
