package uk.gov.hmcts.cmc.domain.models.ccj;

public enum PaymentOption {
    IMMEDIATELY("immediately"),
    FULL_BY_SPECIFIED_DATE("on or before %s"),
    INSTALMENTS("by instalments");

    String description;

    PaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
