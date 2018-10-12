package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class AmountContent {

    private String totalAmount;
    private String subTotalAmount;
    private InterestContent interest;
    private String feeAmount;
    private String paidAmount;
    private String remainingAmount;
    private String admittedAmount;
    private boolean partAdmissionResponse;

    public AmountContent(
        String totalAmount,
        String subTotalAmount,
        InterestContent interest,
        String feeAmount,
        String paidAmount,
        String remainingAmount,
        String admittedAmount,
        boolean partAdmissionResponse
    ) {
        this.totalAmount = totalAmount;
        this.subTotalAmount = subTotalAmount;
        this.interest = interest;
        this.feeAmount = feeAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
        this.admittedAmount = admittedAmount;
        this.partAdmissionResponse = partAdmissionResponse;
    }

    public InterestContent getInterest() {
        return interest;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getSubTotalAmount() {
        return subTotalAmount;
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

    public String getAdmittedAmount() {
        return admittedAmount;
    }

    public boolean isPartAdmissionResponse() {
        return partAdmissionResponse;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
