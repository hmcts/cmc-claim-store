package uk.gov.hmcts.cmc.claimstore.services.staff.models;

public class RepaymentPlanContent {
    private String paymentType;
    private String instalmentAmount;
    private String firstPaymentDate;
    private String paymentSchedule;

    public RepaymentPlanContent(String paymentType, String instalmentAmount, String firstPaymentDate, String paymentSchedule) {
        this.paymentType = paymentType;
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = paymentSchedule;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getInstalmentAmount() {
        return instalmentAmount;
    }

    public String getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public String getPaymentSchedule() {
        return paymentSchedule;
    }
}
