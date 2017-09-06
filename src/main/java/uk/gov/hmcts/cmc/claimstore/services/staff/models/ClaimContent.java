package uk.gov.hmcts.cmc.claimstore.services.staff.models;

public class ClaimContent {

    private final String referenceNumber;
    private final String submittedOn;
    private final String issuedOn;
    private final String reason;
    private final String claimAmount;
    private final String feeAmount;
    private final InterestContent interest;
    private final String claimTotalAmount;

    @SuppressWarnings("squid:S00107") // Suppressed due to MVP timelines, require more time to investigate and fix
    public ClaimContent(
        String referenceNumber,
        String submittedOn,
        String issuedOn,
        String reason,
        String claimAmount,
        String feeAmount,
        InterestContent interest,
        String claimTotalAmount) {
        this.referenceNumber = referenceNumber;
        this.submittedOn = submittedOn;
        this.issuedOn = issuedOn;
        this.reason = reason;
        this.claimAmount = claimAmount;
        this.feeAmount = feeAmount;
        this.interest = interest;
        this.claimTotalAmount = claimTotalAmount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getSubmittedOn() {
        return submittedOn;
    }

    public String getIssuedOn() {
        return issuedOn;
    }

    public String getReason() {
        return reason;
    }

    public String getClaimAmount() {
        return claimAmount;
    }

    public String getFeeAmount() {
        return feeAmount;
    }

    public InterestContent getInterest() {
        return interest;
    }

    public String getClaimTotalAmount() {
        return claimTotalAmount;
    }

}
