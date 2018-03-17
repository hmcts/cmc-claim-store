package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import org.springframework.util.StringUtils;

public class RepaymentPlanContent {
    private String paymentType;
    private String instalmentAmount;
    private String firstPaymentDate;
    private String paymentSchedule;

    public RepaymentPlanContent(String paymentType, String instalmentAmount, String firstPaymentDate, String paymentSchedule) {
        this.paymentType = StringUtils.capitalize(paymentType);
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = StringUtils.capitalize(paymentSchedule);
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
