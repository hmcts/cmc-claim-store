package uk.gov.hmcts.cmc.domain.models;

public enum PaymentOption {
    IMMEDIATELY("Immediately"),
    FULL_BY_SPECIFIED_DATE("By a set date"),
    INSTALMENTS("By instalments");

    String description;

    PaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
