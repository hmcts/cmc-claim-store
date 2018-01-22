package uk.gov.hmcts.cmc.ccd.domain.ccj;

public enum CCDPaymentOption {
    IMMEDIATELY("immediately"),
    FULL_BY_SPECIFIED_DATE("on or before specified date"),
    INSTALMENTS("by instalments");

    String description;

    CCDPaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
