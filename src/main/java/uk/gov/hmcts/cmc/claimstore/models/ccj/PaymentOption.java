package uk.gov.hmcts.cmc.claimstore.models.ccj;

public enum PaymentOption {
    IMMEDIATELY("immediately"),
    FULL_BY_SPECIFIED_DATE("on or before a set date"),
    INSTALMENTS("by instalments");

    String description;

    PaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
