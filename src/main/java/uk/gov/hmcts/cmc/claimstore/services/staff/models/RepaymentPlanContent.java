package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import org.springframework.util.StringUtils;

public class RepaymentPlanContent {
    private final String repaymentOption;
    private String instalmentAmount;
    private String firstPaymentDate;
    private String paymentSchedule;
    private String paySetByDate;

    public RepaymentPlanContent(String repaymentOption) {
        this.repaymentOption = repaymentOption;
    }

    public RepaymentPlanContent(String repaymentOption, String paySetByDate) {
        this.repaymentOption = repaymentOption;
        this.paySetByDate = paySetByDate;
    }

    public RepaymentPlanContent(
        String repaymentOption,
        String instalmentAmount,
        String firstPaymentDate,
        String paymentSchedule) {
        this.repaymentOption = repaymentOption;
        this.instalmentAmount = instalmentAmount;
        this.firstPaymentDate = firstPaymentDate;
        this.paymentSchedule = StringUtils.capitalize(paymentSchedule);
    }

    public String getRepaymentOption() {
        return repaymentOption;
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

    public String getPaySetByDate() {
        return paySetByDate;
    }
}
