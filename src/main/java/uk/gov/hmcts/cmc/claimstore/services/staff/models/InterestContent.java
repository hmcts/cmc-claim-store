package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import java.math.BigDecimal;

public class InterestContent {

    private String rate;
    private Boolean customRate;
    private String customRateReason;
    private Boolean customFromDate;
    private String fromDate;
    private String amount;
    private BigDecimal amountRealValue;
    private String dailyAmount;

    @SuppressWarnings("squid:S00107")
    public InterestContent(
        String rate,
        Boolean customRate,
        String customRateReason,
        Boolean customFromDate,
        String fromDate,
        String amount,
        BigDecimal amountRealValue,
        String dailyAmount) {
        this.rate = rate;
        this.customRate = customRate;
        this.customRateReason = customRateReason;
        this.customFromDate = customFromDate;
        this.fromDate = fromDate;
        this.amount = amount;
        this.amountRealValue = amountRealValue;
        this.dailyAmount = dailyAmount;
    }

    public InterestContent(
        String rate,
        String fromDate,
        String amount,
        String dailyAmount) {
        this.rate = rate;
        this.fromDate = fromDate;
        this.amount = amount;
        this.dailyAmount = dailyAmount;
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

}
