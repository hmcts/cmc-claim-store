package uk.gov.hmcts.cmc.claimstore.services.staff.models;

public class InterestBreakdownContent {
    private String totalAmount;
    private String explanation;

    public InterestBreakdownContent(String totalAmount, String explanation) {
        this.totalAmount = totalAmount;
        this.explanation = explanation;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getExplanation() {
        return explanation;
    }
}
