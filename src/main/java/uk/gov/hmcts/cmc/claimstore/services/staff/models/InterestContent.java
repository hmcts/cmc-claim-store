package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import java.math.BigDecimal;

public class InterestContent {

    private final String type;
    private String rate;
    private Boolean customRate;
    private String customRateReason;
    private InterestBreakdownContent interestBreakdown;
    private Boolean customFromDate;
    private String fromDate;
    private String amount;
    private BigDecimal amountRealValue;
    private final String dailyAmount;
    private String startDateReason;
    private Boolean submissionEndDate;
    private String interestEndDateType;

    @SuppressWarnings("squid:S00107")
    public InterestContent(
        String type,
        String rate,
        Boolean customRate,
        String customRateReason,
        Boolean customFromDate,
        String fromDate,
        String amount,
        BigDecimal amountRealValue,
        String dailyAmount,
        String startDateReason,
        Boolean submissionEndDate
    ) {
        this.type = type;
        this.rate = rate;
        this.customRate = customRate;
        this.customRateReason = customRateReason;
        this.customFromDate = customFromDate;
        this.fromDate = fromDate;
        this.amount = amount;
        this.amountRealValue = amountRealValue;
        this.dailyAmount = dailyAmount;
        this.startDateReason = startDateReason;
        this.submissionEndDate = submissionEndDate;
    }

    public InterestContent(
        String type,
        String rate,
        String fromDate,
        String amount,
        String dailyAmount
    ) {
        this.type = type;
        this.rate = rate;
        this.fromDate = fromDate;
        this.amount = amount;
        this.dailyAmount = dailyAmount;
    }

    public InterestContent(
        String type,
        InterestBreakdownContent interestBreakdown,
        String dailyAmount,
        String interestEndDateType,
        BigDecimal amountRealValue
    ) {
        this.type = type;
        this.interestBreakdown = interestBreakdown;
        this.dailyAmount = dailyAmount;
        this.interestEndDateType = interestEndDateType;
        this.amountRealValue = amountRealValue;
    }

    public String getType() {
        return type;
    }

    public InterestBreakdownContent getInterestBreakdown() {
        return interestBreakdown;
    }

    public String getInterestEndDateType() {
        return interestEndDateType;
    }

    public String getRate() {
        return rate;
    }

    public Boolean isCustomRate() {
        return customRate;
    }

    public String getCustomRateReason() {
        return customRateReason;
    }

    public Boolean isCustomFromDate() {
        return customFromDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getAmount() {
        return amount;
    }

    public BigDecimal getAmountRealValue() {
        return amountRealValue;
    }

    public String getDailyAmount() {
        return dailyAmount;
    }

    public String getStartDateReason() {
        return startDateReason;
    }

    public Boolean getSubmissionEndDate() {
        return submissionEndDate;
    }

}
