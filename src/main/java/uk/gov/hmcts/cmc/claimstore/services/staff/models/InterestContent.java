package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import java.math.BigDecimal;

public class InterestContent {

    private String rate;
    private Boolean customRate;
    private String customRateReason;
    private Boolean customFromDate;
    private String fromDate;
    private String amountUpToNow;
    private BigDecimal amountUpToNowRealValue;
    private String dailyAmount;

    @SuppressWarnings("squid:S00107")
    public InterestContent(
        String rate,
        Boolean customRate,
        String customRateReason,
        Boolean customFromDate,
        String fromDate,
        String amountUpToNow,
        BigDecimal amountUpToNowRealValue,
        String dailyAmount) {
        this.rate = rate;
        this.customRate = customRate;
        this.customRateReason = customRateReason;
        this.customFromDate = customFromDate;
        this.fromDate = fromDate;
        this.amountUpToNow = amountUpToNow;
        this.amountUpToNowRealValue = amountUpToNowRealValue;
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

    public String getAmountUpToNow() {
        return amountUpToNow;
    }

    public BigDecimal getAmountUpToNowRealValue() {
        return amountUpToNowRealValue;
    }

    public String getDailyAmount() {
        return dailyAmount;
    }

}
