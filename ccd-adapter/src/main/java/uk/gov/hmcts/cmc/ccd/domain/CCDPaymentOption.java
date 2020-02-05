package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDPaymentOption {
    IMMEDIATELY("immediately"),
    BY_SPECIFIED_DATE("on or before specified date"),
    INSTALMENTS("by instalments");

    final String description;

    CCDPaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
